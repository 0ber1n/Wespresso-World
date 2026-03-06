package io.wespresso_world.wespresso_world.beans;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
 
@Tag(name = "Beans API", description = "Endpoints for managing coffee beans") // Adds OpenAPI tag for grouping endpoints in documentation
@RestController
@RequestMapping("/beans")
public class BeansController {
    private final BeansRepository beansRepository;

    public BeansController(BeansRepository BeansRepository) {
        this.beansRepository = BeansRepository;
    }

    @Operation(summary = "Get all coffee beans", description = "Returns a list of all coffee beans in the inventory") // Adds OpenAPI operation summary and description for API documentation
    @GetMapping
    public List<Beans> getAllBeans() {
        return beansRepository.findAll();
    }

    @Operation(summary = "Create a new coffee bean", description = "Creates a new coffee bean in the inventory") // Adds OpenAPI operation summary and description for API documentation
    @PostMapping
    public Beans createBeans(@RequestBody Beans beans) {
        return beansRepository.save(beans);
    }

    @Operation(summary = "Delete a coffee bean", description = "Deletes a coffee bean from the inventory") // Adds OpenAPI operation summary and description for API documentation  
    @DeleteMapping("/{id}")
    public void deleteBeans(@PathVariable Long id) {
        beansRepository.deleteById(id);
    }    

    @Operation(summary = "Update a coffee bean", description = "Partially updates a coffee bean in the inventory") // Adds OpenAPI operation summary and description for API documentation
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