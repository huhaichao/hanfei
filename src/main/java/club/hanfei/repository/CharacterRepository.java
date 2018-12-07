
package club.hanfei.repository;

import club.hanfei.model.Character;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Character repository.
 *
@version 1.0.0.0, Jun 8, 2016
 * @since 1.4.0
 */
@Repository
public class CharacterRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public CharacterRepository() {
        super(Character.CHARACTER);
    }
}
