
package club.hanfei.model;

import club.hanfei.util.Hanfei;
import org.json.JSONObject;

/**
 * This class defines all liveness model relevant keys.
 *
@version 1.1.0.0, Jun 12, 2018
 * @since 1.4.0
 */
public final class Liveness {

    /**
     * Liveness.
     */
    public static final String LIVENESS = "liveness";

    /**
     * Key of user id.
     */
    public static final String LIVENESS_USER_ID = "livenessUserId";

    /**
     * Key of liveness date.
     */
    public static final String LIVENESS_DATE = "livenessDate";

    /**
     * Key of liveness point.
     */
    public static final String LIVENESS_POINT = "livenessPoint";

    /**
     * Key of liveness article.
     */
    public static final String LIVENESS_ARTICLE = "livenessArticle";

    /**
     * Key of liveness comment.
     */
    public static final String LIVENESS_COMMENT = "livenessComment";

    /**
     * Key of liveness activity.
     */
    public static final String LIVENESS_ACTIVITY = "livenessActivity";

    /**
     * Key of liveness thank.
     */
    public static final String LIVENESS_THANK = "livenessThank";

    /**
     * Key of liveness vote.
     */
    public static final String LIVENESS_VOTE = "livenessVote";

    /**
     * Key of liveness reward.
     */
    public static final String LIVENESS_REWARD = "livenessReward";

    /**
     * Key of liveness PV.
     */
    public static final String LIVENESS_PV = "livenessPV";

    /**
     * Key of liveness accept answer.
     */
    public static final String LIVENESS_ACCEPT_ANSWER = "livenessAcceptAnswer";

    /**
     * Calculates point of the specified liveness.
     *
     * @param liveness the specified liveness
     * @return point
     */
    public static int calcPoint(final JSONObject liveness) {
        final float activityPer = Hanfei.getFloat("activitYesterdayLivenessReward.activity.perPoint");
        final float articlePer = Hanfei.getFloat("activitYesterdayLivenessReward.article.perPoint");
        final float commentPer = Hanfei.getFloat("activitYesterdayLivenessReward.comment.perPoint");
        final float pvPer = Hanfei.getFloat("activitYesterdayLivenessReward.pv.perPoint");
        final float rewardPer = Hanfei.getFloat("activitYesterdayLivenessReward.reward.perPoint");
        final float thankPer = Hanfei.getFloat("activitYesterdayLivenessReward.thank.perPoint");
        final float votePer = Hanfei.getFloat("activitYesterdayLivenessReward.vote.perPoint");
        final float acceptAnswerPer = Hanfei.getFloat("activitYesterdayLivenessReward.acceptAnswer.perPoint");

        final int activity = liveness.optInt(Liveness.LIVENESS_ACTIVITY);
        final int article = liveness.optInt(Liveness.LIVENESS_ARTICLE);
        final int comment = liveness.optInt(Liveness.LIVENESS_COMMENT);
        int pv = liveness.optInt(Liveness.LIVENESS_PV);
        if (pv > 50) {
            pv = 50;
        }
        final int reward = liveness.optInt(Liveness.LIVENESS_REWARD);
        final int thank = liveness.optInt(Liveness.LIVENESS_THANK);
        int vote = liveness.optInt(Liveness.LIVENESS_VOTE);
        if (vote > 10) {
            vote = 10;
        }
        final int acceptAnswer = liveness.optInt(Liveness.LIVENESS_ACCEPT_ANSWER);

        final int activityPoint = (int) (activity * activityPer);
        final int articlePoint = (int) (article * articlePer);
        final int commentPoint = (int) (comment * commentPer);
        final int pvPoint = (int) (pv * pvPer);
        final int rewardPoint = (int) (reward * rewardPer);
        final int thankPoint = (int) (thank * thankPer);
        final int votePoint = (int) (vote * votePer);
        final int acceptAnswerPoint = (int) (acceptAnswer * acceptAnswerPer);

        int ret = activityPoint + articlePoint + commentPoint + pvPoint + rewardPoint + thankPoint + votePoint + acceptAnswerPoint;

        final int max = Hanfei.getInt("activitYesterdayLivenessReward.maxPoint");
        if (ret > max) {
            ret = max;
        }

        return ret;
    }

    /**
     * Private constructor.
     */
    private Liveness() {
    }
}
