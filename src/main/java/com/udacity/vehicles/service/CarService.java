package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    @Autowired
    CarRepository repository;
    @Autowired
    MapsClient mapWebClient;
    @Autowired
    PriceClient pricingWebClient;

    public CarService(CarRepository repository, MapsClient mapWebClient, PriceClient pricingWebClient) {

        this.pricingWebClient = pricingWebClient;
        this.mapWebClient = mapWebClient;
        this.repository = repository;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        List<Car> carList =  repository.findAll();
        for(Car car : carList) {
            car.setPrice(pricingWebClient
                    .getPrice(car.getId().toString()));

            car.setLocation(mapWebClient
                    .getAddress(car.getLocation()));
        }

        return carList;
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> carById = repository.findById(id);
        if(!carById.isPresent()) {
            throw new CarNotFoundException("Car not found!");
        }
        Car car = carById.get();

        car.setPrice(pricingWebClient
                .getPrice(id.toString()));

        car.setLocation(mapWebClient
                .getAddress(car.getLocation()));

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            Optional<Car> carToUpdate = repository.findById(car.getId());
            if(!carToUpdate.isPresent()) {
                throw new CarNotFoundException("Car not found!");
            }
            carToUpdate.get().setCondition(car.getCondition());
            carToUpdate.get().setDetails(car.getDetails());
            carToUpdate.get().setLocation(car.getLocation());
            carToUpdate.get().setPrice(car.getPrice());
            return repository.save(carToUpdate.get());
        }
        else {
            return repository.save(car);
        }
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> carToDelete = repository.findById(id);
        if(!carToDelete.isPresent()) {
            throw new CarNotFoundException("Car not found!");
        }
        repository.delete(carToDelete.get());
    }
}
