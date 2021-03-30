package io.microservices.moviecatalogservices.resources;

import io.microservices.moviecatalogservices.models.CatalogItem;
import io.microservices.moviecatalogservices.models.Movie;
import io.microservices.moviecatalogservices.models.Rating;
import io.microservices.moviecatalogservices.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    WebClient.Builder webClientBuilder;

    //@RequestMapping(value = "/{userId}",produces = {"application/XML","application/JSON"})
    //@GetMapping(path = "/{userId}", produces = {"application/xml"})
    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId")  String userId)
    {

        //Get all Rated movies IDs -This is coming from rating-data-service API call
       // UserRating ratings=restTemplate.getForObject("http://localhost:8083/ratingsdata/users/"+userId, UserRating.class);//hard coded url
        UserRating ratings=restTemplate.getForObject("http://rating-data-service/ratingsdata/users/"+userId, UserRating.class);


        return   ratings.getUserRating().stream().map(rating -> {
          //1. Using RestTemplate
          //For each Movie Id,call movie info service and get details
           //Movie movie=restTemplate.getForObject("http://localhost:8082/movies/"+rating.getMovieId(), Movie.class);//hard coded url
            Movie movie=restTemplate.getForObject("http://movie-info-service/movies/"+rating.getMovieId(), Movie.class);
          //put them all together
           return new CatalogItem(movie.getName(),"Desc",rating.getRating());

    })
      .collect(Collectors.toList());

    }
}

//2. Using WebClient (put all code inside map)
//webClientBuilder.build() : Using an pattern and giving a webClient
//get(): what type of request get(),post(),put() and delete()
//uri("http://localhost:8082/movies/"+rating.getMovieId()) : this means that you need to access for get request
//where the request to be made.
//retrieve():go to the fetch
//bodyToMono(Movie.class): whatever body you get convert into Movie class (getting asynchronous object)
//block(): We are blocking execution till mono has fulfilled.
/*
          Movie movie= webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/movies/"+rating.getMovieId())
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
           //put them all together
         return new CatalogItem(movie.getName(),"Desc",rating.getRating());*/
