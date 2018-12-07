
package club.hanfei.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

/**
 * Reddit score algorithm test.

 * @version 1.0.0.0, Mar 27, 2017
 * @since 2.1.0
 */
public class RedditScoreTest {

    /**
     * Reddit Score compute mock
     */
    @Test
    public void redditScore(){
        List<vote> votes=new ArrayList<vote>();
        Random random=new Random();
        for(int i=0;i<20;i++){
            int up=random.nextInt(5);
            int down=random.nextInt(5);
            double score=redditCommentScore(up,down);
            votes.add(new vote(i,up,down,score));
        }
        Collections.sort(votes);
        System.out.println(votes.toString());
    }

    private double redditCommentScore(final int ups, final int downs) {
        final int n = ups + downs;
        if (0 == n) {
            return 0;
        }
        final double z = 1.281551565545; // 1.0: 85%, 1.6: 95%, 1.281551565545: 80%
        final double p = (double) ups / n;

        return (p + z * z / (2 * n) - z * Math.sqrt((p * (1 - p) + z * z / (4 * n)) / n)) / (1 + z * z / n);
    }
}
class vote implements Comparable<vote>{
    int id;
    int up;
    int down;
    double score;
    public vote(){

    }
    public vote(int id,int up,int down,double score){
        this.id=id;
        this.up=up;
        this.down=down;
        this.score=score;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(vote v) {
        if(this.score>v.score){
            return -1;
        }else if(this.score<v.score){
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public String toString(){
        return this.id+" : "+this.up+" , "+this.down+" , "+this.score+"\n";
    }
}
