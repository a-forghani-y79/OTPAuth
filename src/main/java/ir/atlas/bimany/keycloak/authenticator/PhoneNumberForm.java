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

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.LoginFormsUtil;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

public final class PhoneNumberForm extends UsernamePasswordForm {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (context.getUser() != null) {
            // We can skip the form when user is re-authenticating. Unless current user has some IDP set, so he can re-authenticate with that IDP
            List<IdentityProviderModel> identityProviders = LoginFormsUtil
                    .filterIdentityProviders(context.getRealm().getIdentityProvidersStream(), context.getSession(), context);
            if (identityProviders.isEmpty()) {
                context.success();
                return;
            }
        }
        super.authenticate(context);
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return this.validateUser(context, formData);
    }

    public boolean validateUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        UserModel user = this.getUser(context, inputData);
        return user != null && validateUser(context, user, inputData);
    }

    private UserModel getUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        if (this.isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            UserModel user = context.getUser();
            this.testInvalidUser(context, user);
            return user;
        } else {
            context.clearUser();
            return this.getUserFromForm(context, inputData);
        }
    }

    private boolean validateUser(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {
        if (!this.enabledUser(context, user)) {
            return false;
        } else {
            String rememberMe = (String) inputData.getFirst("rememberMe");
            boolean remember = context.getRealm().isRememberMe() && rememberMe != null && rememberMe.equalsIgnoreCase("on");
            if (remember) {
                context.getAuthenticationSession().setAuthNote("remember_me", "true");
                context.getEvent().detail("remember_me", "true");
            } else {
                context.getAuthenticationSession().removeAuthNote("remember_me");
            }

            context.setUser(user);
            return true;
        }
    }

    private UserModel getUserFromForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = (String) inputData.getFirst("username");
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
                user = context
                        .getSession()
                        .users()
                        .searchForUserByUserAttributeStream(context.getRealm(), context
                                .getAuthenticatorConfig()
                                .getConfig()
                                .get("CONF_PRP_USR_ATTR_MOBILE"), username)
                        .findFirst()
                        .orElseThrow(Exception::new);
            } catch (ModelDuplicateException var6) {
                ServicesLogger.LOGGER.modelDuplicateException(var6);
                if (var6.getDuplicateFieldName() != null && var6.getDuplicateFieldName().equals("email")) {
                    this.setDuplicateUserChallenge(context, "email_in_use", "emailExistsMessage", AuthenticationFlowError.INVALID_USER);
                } else {
                    this.setDuplicateUserChallenge(context, "username_in_use", "usernameExistsMessage", AuthenticationFlowError.INVALID_USER);
                }

                return user;
            } catch (Exception e) {
                this.setDuplicateUserChallenge(context, "username_in_use", "usernameExistsMessage", AuthenticationFlowError.UNKNOWN_USER);
            }

            this.testInvalidUser(context, user);
            return user;
        }
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsername();
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        if (context.getRealm().isLoginWithEmailAllowed())
            return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }
}
