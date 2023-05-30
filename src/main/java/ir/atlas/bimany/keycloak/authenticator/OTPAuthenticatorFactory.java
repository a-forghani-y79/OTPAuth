package ir.atlas.bimany.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class OTPAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "bimany-otp-authenticator";
    private static final OTPAuthenticator SINGLETON = new OTPAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;


        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        property.setLabel("CONF_PRP_USR_ATTR_MOBILE");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Mobile number attribute in the user model.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL);
        property.setLabel("CONF_PRP_SMS_CODE_TTL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS OTP Time-To-Live in sec.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH);
        property.setLabel("CONF_PRP_SMS_CODE_LENGTH");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS OTP length.");
        configProperties.add(property);

    }


    @Override
    public String getHelpText() {
        return "An one time password, that a user has to provide during login.";
    }

    @Override
    public String getDisplayType() {
        return "Bimany OTP via SMS";
    }

    @Override
    public String getReferenceCategory() {
        return "Bimany OTP via SMS";
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }


}
