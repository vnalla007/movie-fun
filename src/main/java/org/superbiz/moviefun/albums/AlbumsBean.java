package org.superbiz.moviefun.albums;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

@Repository
public class AlbumsBean {

    @PersistenceContext(unitName = "albums")
    private EntityManager entityManager;

    public void addAlbum(Album album) {
        entityManager.persist(album);
    }

    public List<Album> getAlbums() {
        CriteriaQuery<Album> cq = entityManager.getCriteriaBuilder().createQuery(Album.class);
        cq.select(cq.from(Album.class));
        return entityManager.createQuery(cq).getResultList();
    }
}