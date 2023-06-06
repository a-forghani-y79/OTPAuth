package ir.atlas.bimany.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Random;

public class OTPAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(OTPAuthenticator.class);

    private static enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }

    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String mobileNumberAttribute = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        if (mobileNumberAttribute == null) {
            Response challenge = context.form()
                    .setError("Phone number could not be determined.")
                    .createForm("bimany-otp-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }
        String mobileNumber = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), mobileNumberAttribute);
        if (mobileNumber != null && !mobileNumber.equals("")) {
            long nrOfDigits = OTPAuthenticatorUtil.getConfigLong(config, OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
            long ttl = OTPAuthenticatorUtil.getConfigLong(config, OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s


            String code = getSmsCode(nrOfDigits);

            storeSMSCode(context, code, new Date().getTime() + (ttl * 1000)); // s --> ms
            if (sendSmsCode(mobileNumber, code, context.getAuthenticatorConfig())) {
                Response challenge = context.form().createForm("bimany-otp.ftl");
                context.challenge(challenge);
            } else {
                Response challenge = context.form()
                        .setError("SMS could not be sent.")
                        .createForm("bimany-otp-error.ftl");
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
                return;
            }
        } else {
            Response challenge = context.form()
                    .setError("SMS could not be sent.")
                    .createForm("bimany-otp-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                challenge = context.form()
                        .setError("The code has been expired.")
                        .createForm("bimany-otp.ftl");
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challenge);
                break;

            case INVALID:
                if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.CONDITIONAL ||
                        context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                    System.out.println("Calling context.attempted()");
                    context.attempted();
                } else if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                    challenge = context.form()
                            .setError("The code is invalid")
                            .createForm("bimany-otp.ftl");
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                } else {
                    // Something strange happened
                    logger.error("Undefined execution ...");
                }
                break;

            case VALID:
                context.success();
                UserModel user = context.getUser();
                user.removeAttribute("otp");
                user.removeAttribute("otp_expiry");
                break;

        }
    }

    private void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
        UserModel user = context.getUser();
        user.setSingleAttribute("otp", code);
        user.setSingleAttribute("otp_expiry", expiringAt.toString());

        context.success();
    }

    private boolean sendSmsCode(String mobileNumber, String code, AuthenticatorConfigModel config) {
        logger.warn("OTPAuthenticator.sendSmsCode");
        logger.warn("mobileNumber = " + mobileNumber + ", code = " + code + ", config = " + config);
        return true;
    }

    private String getSmsCode(long nrOfDigits) {
        if (nrOfDigits < 1) {
            throw new RuntimeException("Nr of digits must be bigger than 0");
        }

        double maxValue = Math.pow(10.0, nrOfDigits); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long) (r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    protected CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.VALID;
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(OTPAuthenticatorContstants.ANSW_SMS_CODE);
        String expectedCode = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), "otp");
        String expTimeString = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), "otp_expiry");
        if (expectedCode != null) {
            result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            long now = new Date().getTime();

            if (result == CODE_STATUS.VALID) {
                if (Long.parseLong(expTimeString) < now) {
                    result = CODE_STATUS.EXPIRED;
                }
            }
        }
        return result;
    }


    @Override
    public boolean requiresUser() {
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}