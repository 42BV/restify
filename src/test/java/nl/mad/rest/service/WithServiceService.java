/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.service;

import nl.mad.rest.model.WithService;
import nl.mad.rest.repository.WithServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithServiceService extends AbstractCrudService<WithService, Long> {
    
    @Autowired
    public WithServiceService(WithServiceRepository repository) {
        super(WithService.class, repository);
    }
    
    @Override
    public <S extends WithService> S save(S entity) {
        entity.setName(entity.getName() + "!");
        return super.save(entity);
    }

}
