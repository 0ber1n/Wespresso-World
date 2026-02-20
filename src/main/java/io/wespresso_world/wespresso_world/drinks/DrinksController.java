package io.wespresso_world.wespresso_world.drinks;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
    public Drinks createDrinks(@RequestBody Drinks Drinks) {
        return drinksRepository.save(Drinks);
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
