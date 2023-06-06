<#import "template.ftl" as layout>
<@layout.registrationLayout ; section>
    <#if section = "header">
        <div>
            <h4>
                ${msg("loginAccountTitle")}
            </h4>
        </div>
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
                      method="post">
                    <h2>${msg("enter-phone-number")}</h2>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="phoneNumber"
                               class="${properties.kcLabelClass!}">${msg("phone-number-label")}</label>

                        <input tabindex="1" id="phoneNumber" class="${properties.kcInputClass!}" name="phoneNumber"
                               type="text" autofocus autocomplete="off"
                               aria-invalid="true"
                        />
                    </div>
                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <div class="${properties.kcFormButtonsWrapperClass!}">
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                                   name="login" id="kc-login" type="submit" value="${msg("doNext")}"/>
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                                   name="cancel" id="kc-cancel" type="submit" value="${msg("doCancel")}"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </#if>

</@layout.registrationLayout>
