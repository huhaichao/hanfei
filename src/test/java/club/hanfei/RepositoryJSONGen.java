
package club.hanfei;

import java.util.Arrays;
import java.util.HashSet;

import org.b3log.latke.repository.jdbc.util.JdbcRepositories;

/**
 * Database reverse generation case.
 *
@version 1.0.0.0, Sep 1, 2018
 * @since 3.4.0
 */
public class RepositoryJSONGen {

    public static void main(final String[] args) {
        JdbcRepositories.initRepositoryJSON("symphony_", new HashSet<>(Arrays.asList("article")), "reverse-repository.json");
    }
}
