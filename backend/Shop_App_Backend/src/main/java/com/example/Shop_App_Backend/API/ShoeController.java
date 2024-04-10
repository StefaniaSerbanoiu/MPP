package com.example.Shop_App_Backend.API;
/*
import com.example.Shop_App_Backend.Domain.Client;
import com.example.Shop_App_Backend.Domain.Shoe;
import com.example.Shop_App_Backend.Domain.Transaction; */
import com.example.Shop_App_Backend.Domain.Shoe;
import com.example.Shop_App_Backend.Service.ShoeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/shoe")
@AllArgsConstructor
@Validated // This annotation is used at the controller level to enable method validation.
@CrossOrigin(origins = "http://localhost:5173") // Specifying frontend origin
public class ShoeController {
    private ShoeService service;

    @PostMapping("/add")
    public Shoe add(@Valid @RequestBody Shoe newEntity) { return this.service.addService(newEntity); }

    @PostMapping("/all")
    public List<Shoe> addAll(@Valid @RequestBody List<Shoe> entities)
    {
        return this.service.addAllService(entities);
    }

    public ResponseEntity<Object> showMessage(Object messageOrEntity, HttpStatus status)
    {
        return ResponseEntity.status(status).body(messageOrEntity);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll()
    {
        List<Shoe> shoes = this.service.getAll();
        if(shoes.isEmpty())
        {
            // there are no shoes saved in the database
            return this.showMessage("There are no shoes yet.", HttpStatus.NOT_FOUND); // 404
        }
        // if there are shoes in the database, a list of them is returned
        return ResponseEntity.ok(shoes); // 200
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") String id)
    {
        if(Objects.equals(id, "undefined"))
            id = "2";
        Shoe shoe = this.service.getEntityById(Integer.parseInt(id));
        if(shoe != null)
        {
            // the shoe was found
            return ResponseEntity.ok(shoe); // 200
        }
        else
        {
            // the shoe was not found
            String errorMessage = "Shoe with id " + id + " was not found.";
            return this.showMessage(errorMessage, HttpStatus.NOT_FOUND); // 404
        }
    }

    /*
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Object> getTransactionsWithShoe(@PathVariable("id") Integer shoeId)
    {
        Shoe shoe = this.service.getEntityById(shoeId);
        if(shoe != null)
        {
            // the shoe exists
            Set<Transaction> transactionSet = this.service.getTransactionsForShoe(shoeId);
            if(!transactionSet.isEmpty())
            {
                return this.showMessage(transactionSet, HttpStatus.OK); // 200
            }
            else
            {
                // the shoe was not ordered yet
                return this.showMessage("There was not any shoe with id " + shoeId + " which " +
                        "was ordered until now.", HttpStatus.NOT_FOUND); // 404
            }
        }
        // the shoe was not found
        return this.showMessage("There is no shoe with id " + shoeId + ".", HttpStatus.NOT_FOUND);
        // 404 status
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<Object> getClients(@PathVariable("id") Integer shoeId)
    {
        Shoe shoe = this.service.getEntityById(shoeId);
        if(shoe != null)
        {
            // the shoe exists
            Set<Client> clients = this.service.getClientsWhoBoughtShoe(shoeId);
            if(!clients.isEmpty())
            {
                return this.showMessage(clients, HttpStatus.OK); // 200
            }
            else
            {
                // the shoe was not ordered yet
                return this.showMessage("There was not any shoe with id " + shoeId + " which " +
                        "was ordered until now.", HttpStatus.NOT_FOUND); // 404
            }
        }
        // the shoe was not found
        return this.showMessage("There is no shoe with id " + shoeId + ".", HttpStatus.NOT_FOUND);
        // 404 status
    }
     */

    @GetMapping("/filter/{valueForFiltering}")
    public ResponseEntity<Object> filter(@PathVariable("valueForFiltering") Integer valueForFiltering)
    {
        List<Shoe> shoes = this.service.filterByQuantity(valueForFiltering);
        if(shoes.isEmpty())
        {
            // there are no shoes matching the filter criteria
            String errorMessage = "There are no shoes with a quantity bigger than " + valueForFiltering + ".";
            return this.showMessage(errorMessage, HttpStatus.NOT_FOUND); // 404
        }
        return showMessage(shoes, HttpStatus.OK); // 200
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Integer id)
    {
        String deleteMessage;
        HttpStatus status;
        if(this.service.exists(id))
        {
            // the shoe was found and deleted
            this.service.deleteService(id);
            deleteMessage = "Shoe with id " + id + " was successfully deleted.";
            status = HttpStatus.OK; // 200
        }
        else
        {
            // the shoe was not found and the deletion can't be done
            deleteMessage = "Shoe with id " + id + " was not found.";
            status = HttpStatus.NOT_FOUND; // 404
        }
        return this.showMessage(deleteMessage, status);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Integer id, @Valid @RequestBody Shoe entity)
    {
        Shoe shoe = this.service.updateService(id, entity);
        if(shoe != null)
        {
            // the shoe was found and updated
            return ResponseEntity.status(HttpStatus.OK).body(shoe); // 200
        }
        else
        {
            // the shoe was not found and the update operation cannot be completed
            String errorMessage = "The shoe with id " + id + " was not found.";
            return this.showMessage(errorMessage, HttpStatus.NOT_FOUND); // 404
        }
    }


    @MessageMapping("/cronAdd")
    @SendTo("/topic/cronjob")
    public void cronJob() {
        addEntity();
    }

    @Scheduled(fixedRate = 10000)
    public void addEntity() {
        Random rand = new Random();
        // Generate random shoe entities
        String[] sizes = {"34", "36", "38", "40"};

        Random random = new Random();

        String productName = generateRandomName(10); // Generate random 10-letter name
        int price = random.nextInt(200) + 50; // Random price between 50 and 250
        int size = Integer.parseInt(sizes[random.nextInt(sizes.length)]);

        // Now you can create a Shoe object with these random values
        Shoe shoe = new Shoe();
        shoe.setShoe_id(rand.nextInt(1000));
        shoe.setProduct_name(productName);
        shoe.setSize(size);
        shoe.setPrice(price);

        service.addService(shoe);
    }



    // Method to generate a random string of given length
    private String generateRandomName(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char randomChar = (char) ('a' + random.nextInt(26));
            sb.append(randomChar);
        }
        return sb.toString();
    }

}
