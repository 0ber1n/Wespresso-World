package io.wespresso_world.wespresso_world.drinks;

import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/menu")
public class DrinksController {
    private final DrinksRepository drinksRepository;

    public DrinksController(DrinksRepository DrinksRepository) {
        this.drinksRepository = DrinksRepository;
    }

    @GetMapping
    public List<Drinks> getAllDrinkss() {
        return drinksRepository.findAll();
    }

    @PostMapping
    public Drinks createDrinks(@RequestBody Drinks drinks) {
        return drinksRepository.save(drinks);
    }   
    
    @DeleteMapping("/{id}")
    public void deleteDrinks(@PathVariable Long id) {
        drinksRepository.deleteById(id);
    }


    // PUT is for full updates, PATCH is for partial updates. PUT nulls out values not included in body.
    @PutMapping("/{id}")
    public Drinks updateDrinks(@PathVariable Long id, @RequestBody Drinks updatedDrinks) {
        return drinksRepository.findById(id)
                .map(Drinks -> {
                    Drinks.setName(updatedDrinks.getName());
                    Drinks.setDescription(updatedDrinks.getDescription());
                    Drinks.setPrice(updatedDrinks.getPrice());
                    return drinksRepository.save(Drinks);
                })
                .orElseGet(() -> {
                    return drinksRepository.save(updatedDrinks);
                });
    }
    
    @PatchMapping("/{id}")
    public Drinks partialUpdateDrinks(@PathVariable Long id, @RequestBody Drinks updatedDrinks) {
        return drinksRepository.findById(id)
                .map(Drinks -> {
                    if (updatedDrinks.getName() != null) {
                        Drinks.setName(updatedDrinks.getName());
                    }
                    if (updatedDrinks.getDescription() != null) {
                        Drinks.setDescription(updatedDrinks.getDescription());
                    }
                    if (updatedDrinks.getPrice() != null) {
                        Drinks.setPrice(updatedDrinks.getPrice());
                    }
                    return drinksRepository.save(Drinks);
                })
                .orElseGet(() -> {
                    return drinksRepository.save(updatedDrinks);
                });
    }



}
