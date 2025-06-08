package org.btet.database_repository;
import org.btet.exception.RepositoryAccessException;

import java.util.List;
/**
 * AbstractRepository class is an abstract class that defines the basic operations that can be performed on a repository,
 * such as finding an entity by its username, finding all entities, saving an entity, and saving a list of entities.
 * @param <T> The type of the entity that the repository is managing.
 */
public abstract class AbstractRepository<T> {
    /**
     * Finds an entity by its username.
     * @param username The username of the entity to find.
     * @return The entity with the given username.
     * @throws RepositoryAccessException If an error occurs while accessing the repository.
     */
    abstract T findByUsername(String username) throws RepositoryAccessException;
    /**
     * Finds all entities in the repository.
     * @return A list of all entities in the repository.
     * @throws RepositoryAccessException If an error occurs while accessing the repository.
     */
    abstract List<T> findAll() throws RepositoryAccessException;
    /**
     * Saves a list of entities to the repository, usually combined with {@link #save(T)}.
     * @param entities The list of entities to save.
     * @throws RepositoryAccessException If an error occurs while accessing the repository.
     */
    abstract void save(List<T> entities) throws RepositoryAccessException;
    /**
     * Saves an entity to the repository.
     * @param entity The entity to save.
     * @throws RepositoryAccessException If an error occurs while accessing the repository.
     */
    abstract void save(T entity) throws RepositoryAccessException;
}