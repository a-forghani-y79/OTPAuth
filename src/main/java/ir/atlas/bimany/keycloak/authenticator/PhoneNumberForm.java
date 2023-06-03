/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.atlas.bimany.keycloak.authenticator;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.LoginFormsUtil;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class PhoneNumberForm implements Authenticator {

    private static final String FORM_NAME = "bimany-login.ftl";
    Logger logger = Logger.getLogger(PhoneNumberForm.class.getName());


    @Override
    public boolean requiresUser() {
        logger.warning("PhoneNumberForm.requiresUser");
        return false;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.warning("PhoneNumberForm.authenticate");
        if (context.getUser() != null) {
            // We can skip the form when user is re-authenticating. Unless current user has some IDP set, so he can re-authenticate with that IDP
            List<IdentityProviderModel> identityProviders = LoginFormsUtil
                    .filterIdentityProviders(context.getRealm().getIdentityProvidersStream(), context.getSession(), context);
            if (identityProviders.isEmpty()) {
                context.success();
                return;
            }
        }

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        String loginHint = context.getAuthenticationSession().getClientNote("login_hint");
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());
        if (context.getUser() != null) {
            LoginFormsProvider form = context.form();
            form.setAttribute("usernameHidden", true);
            form.setAttribute("registrationDisabled", true);
            context.getAuthenticationSession().setAuthNote("USER_SET_BEFORE_USERNAME_PASSWORD_AUTH", "true");
        } else {
            context.getAuthenticationSession().removeAuthNote("USER_SET_BEFORE_USERNAME_PASSWORD_AUTH");
            if (loginHint != null || rememberMeUsername != null) {
                if (loginHint != null) {
                    formData.add("username", loginHint);
                } else {
                    formData.add("username", rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
        }

        Response challengeResponse = this.challenge(context, formData);
        context.challenge(challengeResponse);
    }

    private Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        logger.warning("PhoneNumberForm.challenge");
        LoginFormsProvider forms = context.form();
        if (formData.size() > 0) {
            forms.setFormData(formData);
        }

        return forms.createForm(FORM_NAME);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.warning("PhoneNumberForm.action");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
        } else if (this.validateForm(context, formData)) {
            logger.warning("PhoneNumberForm.action - validateForm is true");
            context.success();
        }
    }


    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        logger.warning("PhoneNumberForm.configuredFor");
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        logger.warning("PhoneNumberForm.setRequiredActions");
    }

    @Override
    public void close() {
        logger.warning("PhoneNumberForm.close");

    }


    private String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        logger.warning("PhoneNumberForm.getDefaultChallengeMessage");
        if (context.getRealm().isLoginWithEmailAllowed())
            return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }


    private boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        logger.warning("PhoneNumberForm.validateForm");
        return this.validateUser(context, formData);
    }

    public boolean validateUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        logger.warning("PhoneNumberForm.validateUser");
        UserModel user = this.getUserFromForm(context, inputData);
        context.getAuthenticationSession().setAuthenticatedUser(user);
        return user != null;
    }


    private Response createLoginForm(LoginFormsProvider form) {
        logger.warning("PhoneNumberForm.createLoginForm");
        return form.createForm(PhoneNumberForm.FORM_NAME);
    }


    private UserModel getUserFromForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        logger.warning("PhoneNumberForm.getUserFromForm");
        logger.warning("inputData : ");
        inputData.forEach((s, strings) -> logger.warning(s + " = " + strings));

        String username = (String) inputData.getFirst("phoneNumber");
        if (username == null) {
            context.getEvent().error("user_not_found");
            Response challengeResponse = this.challenge(context, this.getDefaultChallengeMessage(context), "username");
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        } else {
            username = username.trim();
            context.getEvent().detail("username", username);
            context.getAuthenticationSession().setAuthNote("ATTEMPTED_USERNAME", username);
            UserModel user = null;

            try {
//                user = KeycloakModelUtils.(context.getSession(), context.getRealm(), username);

                UserProvider userProvider = context
                        .getSession().users();
                Stream<UserModel> usersStream = userProvider.getUsersStream(context.getRealm(), false);

                logger.warning("users:");
                usersStream.forEach(userModel -> logger.warning("username : " + userModel.getUsername()));


                String confPrpUsrAttrMobile = context
                        .getAuthenticatorConfig()
                        .getConfig()
                        .get("CONF_PRP_USR_ATTR_MOBILE");
                logger.warning("confPrpUsrAttrMobile: " + confPrpUsrAttrMobile);
                logger.warning("try to search user with attr:" + confPrpUsrAttrMobile + " value: " + username);
                logger.warning("cunt users: " + context.getSession().users().getUsersCount(context.getRealm()));

                user = context
                        .getSession()
                        .users()
                        .searchForUserByUserAttributeStream(context.getRealm(), confPrpUsrAttrMobile, username)
                        .findFirst()
                        .orElseThrow(Exception::new);
                logger.warning("user found with Id :" + user.getId());

            } catch (ModelDuplicateException var6) {
                ServicesLogger.LOGGER.modelDuplicateException(var6);
                if (var6.getDuplicateFieldName() != null && var6.getDuplicateFieldName().equals("email")) {
                    this.setDuplicateUserChallenge(context, "email_in_use", "emailExistsMessage", AuthenticationFlowError.INVALID_USER);
                } else {
                    this.setDuplicateUserChallenge(context, "username_in_use", "usernameExistsMessage", AuthenticationFlowError.INVALID_USER);
                }

                return user;
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning(e.getMessage());
                this.setDuplicateUserChallenge(context, "username_in_use", "usernameExistsMessage", AuthenticationFlowError.UNKNOWN_USER);
            }

            return user;
        }
    }

    protected Response setDuplicateUserChallenge(AuthenticationFlowContext context, String eventError, String loginFormError, AuthenticationFlowError authenticatorError) {
        logger.warning("PhoneNumberForm.setDuplicateUserChallenge");
        context.getEvent().error(eventError);
        Response challengeResponse = context.form().setError(loginFormError, new Object[0]).createLoginUsernamePassword();
        context.failureChallenge(authenticatorError, challengeResponse);
        return challengeResponse;
    }

    private Response challenge(AuthenticationFlowContext context, String error, String field) {
        logger.warning("PhoneNumberForm.challenge");
        LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }

        return this.createLoginForm(form);
    }


}
