package io.wespresso_world.wespresso_world.beans;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
 

@RestController
@RequestMapping("/beans")
public class BeansController {
    private final BeansRepository beansRepository;

    public BeansController(BeansRepository BeansRepository) {
        this.beansRepository = BeansRepository;
    }

    @GetMapping
    public List<Beans> getAllBeans() {
        return beansRepository.findAll();
    }

    @PostMapping
    public Beans createBeans(@RequestBody Beans beans) {
        return beansRepository.save(beans);
    }

    @DeleteMapping("/{id}")
    public void deleteBeans(@PathVariable Long id) {
        beansRepository.deleteById(id);
    }    

    // Add PATCH and PUT methods for updating Beans
    
}
