package kr.hhplus.be.server.domain.user;

public interface UserRepository {

    User findById(long id);

    User save(User user);

}