package io.wespresso_world.wespresso_world.beans;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/{id}")
    public Beans partialUpdateBeans(@PathVariable Long id, @RequestBody Beans updatedBeans) {
        return beansRepository.findById(id)
                .map(Beans -> {
                    if (updatedBeans.getName() != null) {
                        Beans.setName(updatedBeans.getName());
                    }
                    if (updatedBeans.getDescription() != null) {
                        Beans.setDescription(updatedBeans.getDescription());
                    }
                    if (updatedBeans.getOrigin() != null) {
                        Beans.setOrigin(updatedBeans.getOrigin());
                    }
                    if (updatedBeans.getRoastLevel() != null) {
                        Beans.setRoastLevel(updatedBeans.getRoastLevel());
                    }
                    if (updatedBeans.getIsRaw() != Beans.getIsRaw()) {
                        Beans.setIsRaw(updatedBeans.getIsRaw());
                    }
                    if (updatedBeans.getPrice() != 0.0) {
                        Beans.setPrice(updatedBeans.getPrice());
                    }
                    return beansRepository.save(Beans);
                })
                .orElseThrow(() -> new RuntimeException("Bean not found with id: " + id));
    }

}