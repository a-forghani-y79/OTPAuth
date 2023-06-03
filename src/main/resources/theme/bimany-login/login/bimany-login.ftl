<#import "template.ftl" as layout>
<@layout.registrationLayout ; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
                      method="post">
                    <input type="hidden" >
                        <h2>Enter PhoneNumber</h2>
                        <div class="${properties.kcFormGroupClass!}">
                            <label for="phoneNumber"
                                   class="${properties.kcLabelClass!}">${msg("phone-number-label")}</label>

                            <input tabindex="1" id="phoneNumber" class="${properties.kcInputClass!}" name="phoneNumber"
                                   type="text" autofocus autocomplete="off"
                                   aria-invalid="true"
                            />
                        </div>
                    <button title="majid btn" onsubmit="majid()">
                        majid btn
                    </button>
                </form>
            </div>
        </div>
    </#if>

</@layout.registrationLayout>
