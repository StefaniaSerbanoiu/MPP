package com.example.Shop_App_Backend.Service;

import com.example.Shop_App_Backend.DTO.ShoeDTO;
import com.example.Shop_App_Backend.Domain.Shoe;
import com.example.Shop_App_Backend.Domain.Suggestion;
import com.example.Shop_App_Backend.Domain.User;
import com.example.Shop_App_Backend.Repository.ShoeRepository;
/*
import com.example.Shop_App_Backend.Domain.Client;
import com.example.Shop_App_Backend.Domain.Transaction;
import com.example.Shop_App_Backend.Repository.ShoeRepository;
import com.example.Shop_App_Backend.Repository.TransactionRepository;
 */
import com.example.Shop_App_Backend.Repository.SuggestionRepository;
import com.example.Shop_App_Backend.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShoeService {
    private ShoeRepository repository;
    private SuggestionRepository suggestionRepository;
    private UserRepository userRepository;

    //private TransactionRepository transactionRepository;

    public Shoe addService(Shoe entity) { return this.repository.save(entity); }

    public List<Shoe> addAllService(List<Shoe> entities) { return this.repository.saveAll(entities); }

    public Shoe getEntityById(Integer id) { return this.repository.findById(id).orElse(null); }

    public List<Shoe> getAll() { return this.repository.findAll(); }

    /*
    public Set<Client> getClientsWhoBoughtShoe(Integer shoeId)
    {
        Shoe entity = this.repository.findById(shoeId).orElse(null);
        if (entity != null)
        {
            Set<Transaction> shoeTransactions = this.getTransactionsForShoe(shoeId);
            if(!shoeTransactions.isEmpty())
            {
                Set<Client> clients = new HashSet<Client>();
                for(Transaction transaction : shoeTransactions)
                {
                    clients.add(transaction.getClient());
                }
                return clients;
            }
            return null;
        }
        return null;
    }

    public Set<Transaction> getTransactionsForShoe(Integer shoeId)
    {
        Shoe shoe = this.getEntityById(shoeId);
        if(shoe != null)
        {
            List<Transaction> transactionsList = this.transactionRepository.findAll();
            if(!transactionsList.isEmpty())
            {
                Set<Transaction> transactionsWithShoe = new HashSet<Transaction>();
                for(Transaction transaction : transactionsList)
                {
                    if(transaction.getShoe().getShoeId() == shoeId)
                    {
                        transactionsWithShoe.add(transaction);
                    }
                }
                return transactionsWithShoe;
            }
            return null;
        }
        return null;
    }
     */

    public boolean exists(Integer id) { return this.repository.existsById(id); }

    public void deleteService(Integer id) { this.repository.deleteById(id); }

    public Shoe updateService(Integer id, Shoe newEntity)
    {
        Shoe entityForUpdate = this.repository.findById(id).orElse(null);
        if(entityForUpdate != null)
        {
            //entityForUpdate.setColor(newEntity.getColor());
            entityForUpdate.setProduct_name(newEntity.getProduct_name());
            entityForUpdate.setPrice(newEntity.getPrice());
            //entityForUpdate.setSeason(newEntity.getSeason());
            //entityForUpdate.setRating(newEntity.getRating());
            //entityForUpdate.setQuantity(newEntity.getQuantity());
            Optional<User> optionalUser = userRepository.findById(newEntity.getUser().getId());
            if(optionalUser.isPresent())
            {
                return this.repository.save(entityForUpdate);
            }
        }
        return null;
    }



    //filtering on a numerical field of Shoe class, bigger than a given value
    //filters all shoes which are available in a size bigger than a given value
    public List<Shoe> filterByQuantity(Integer givenValue)
    {
        return this.repository.findAll().stream().filter(shoe -> shoe.getSize() > givenValue)
                .collect(Collectors.toList());
    }

    public Set<Suggestion> getSuggestionsForShoe(Integer shoeId)
    {
        Shoe shoe = this.getEntityById(shoeId);
        if(shoe != null)
        {
            List<Suggestion> suggestionList = this.suggestionRepository.findAll();
            if(!suggestionList.isEmpty())
            {
                Set<Suggestion> suggestionForShoe = new HashSet<Suggestion>();
                for(Suggestion advice : suggestionList)
                {
                    if(advice.getShoe().getShoe_id() == shoeId)
                    {
                        suggestionForShoe.add(advice);
                    }
                }
                return suggestionForShoe;
            }
            return null;
        }
        return null;
    }

    public List<Shoe> getAllSortedByPrice(Sort.Direction direction)  // sorts by price in increasing or decreasing order
    {
        return this.repository.findAll(Sort.by(direction, "price"));
    }

    public ShoeDTO toShoeDTO(Shoe shoe)
    {
        return new ShoeDTO(shoe.getShoe_id(), shoe.getProduct_name(),
                shoe.getSize(), shoe.getPrice());
    }

    public List<ShoeDTO> getAllByUserService(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User foundUser = user.get();
            List<Shoe> shoes = foundUser.getShoes();
            return shoes.stream()
                    .map(this::toShoeDTO)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
