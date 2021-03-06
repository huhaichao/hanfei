
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${activityLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <div class="module">
                    <div class="module-header">
                        <h2>${activityLabel}</h2>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <li>
                                <div class='fn-flex'>
                                    <div class="avatar tooltipped tooltipped-ne"
                                         aria-label="${activityDailyCheckinLabel}" style="background-image:url('${staticServePath}/images/activities/checkin.png')"></div>
                                    <div class="fn-flex-1">
                                        <h2 class="ft-13">
                                            <a href="<#if useCaptchaCheckin>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a>
                                        </h2>
                                        <span class="ft-fade content-reset">
                                            每日签到随机获得 <code>[${pointActivityCheckinMin?c}, ${pointActivityCheckinMax?c}]</code>，每连续签到 10 天额外获得 <code>${pointActivityCheckinStreak?c}</code>
                                        </span>
                                    </div>
                                </div>
                            </li>
                            <li>
                                <div class='fn-flex'>
                                    <div class="avatar tooltipped tooltipped-ne"
                                         aria-label="${activityYesterdayLivenessRewardLabel}" style="background-image:url('${staticServePath}/images/activities/yesterday.png')"></div>
                                    <div class="fn-flex-1">
                                        <h2 class="ft-13">
                                            <a href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a>
                                        </h2>
                                        <span class="ft-fade content-reset">
                                            领取昨日 <a href="https://hacpai.com/article/1458624687933" class="ft-gray" rel="nofollow">活跃度奖励</a> ，目前最高可以获得 <code>${activitYesterdayLivenessRewardMaxPoint?c}</code>
                                        </span>
                                    </div>
                                </div>
                            </li>
                            <li>
                                <div class='fn-flex'>
                                    <div class="avatar tooltipped tooltipped-ne"
                                         aria-label="${activity1A0001Label}" style="background-image:url('${staticServePath}/images/activities/1A0001.png')"></div>
                                    <div class="fn-flex-1">
                                        <h2 class="ft-13">
                                            <a href="${servePath}/activity/1A0001">${activity1A0001Label}</a>
                                        </h2>
                                        <span class="ft-fade content-reset">
                                            下注后，请在当天 16-24 点在本页面进行兑奖，逾期作废！
                                        </span>
                                    </div>
                                </div>
                            </li>
                            <li>
                                <div class='fn-flex'>
                                    <div class="avatar tooltipped tooltipped-ne"
                                         aria-label="${characterLabel}" style="background-image:url('${staticServePath}/images/activities/char.png')"></div>
                                    <div class="fn-flex-1">
                                        <h2 class="ft-13">
                                            <a href="${servePath}/activity/character">${characterLabel}</a>
                                        </h2>
                                        <span class="ft-fade content-reset">${activityCharacterTitleLabel}</span>
                                    </div>
                                </div>
                            </li>
                            <li>
                                <div class='fn-flex'>
                                    <div class="avatar tooltipped tooltipped-ne"
                                         aria-label="${chatRoomLabel}" style="background-image:url('${staticServePath}/images/activities/chat.png')"></div>
                                    <div class="fn-flex-1">
                                        <h2 class="ft-13">
                                            <a href="${servePath}/cr">${chatRoomLabel}</a>
                                        </h2>
                                        <span class="ft-fade content-reset">${activityChatTitleLabel}</span>
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>