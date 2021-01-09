package org.launchcode.brewpub.controllers;

import org.launchcode.brewpub.models.Pub;
import org.launchcode.brewpub.models.User;
import org.launchcode.brewpub.models.data.BrewRepository;
import org.launchcode.brewpub.models.data.PubRepository;
import org.launchcode.brewpub.models.data.PubReviewRepository;
import org.launchcode.brewpub.models.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("pubs")
public class PubController {

    @Autowired
    private PubRepository pubRepository;

    @Autowired
    private PubReviewRepository pubReviewRepository;

    @Autowired
    private BrewRepository brewRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("")
    public String index(Model model) {
        model.addAttribute("title", "Pubs");
        model.addAttribute("pubs", pubRepository.findAll());
        return "/pubs/index";
    }

    @GetMapping("add")
    public String displayAddPubForm(Model model) {
        model.addAttribute("title", "Add Pub");
        model.addAttribute("pubs", pubRepository.findAll());
        model.addAttribute(new Pub());
        return "/pubs/add";
    }

    @PostMapping("add")
    public String processAddPubForm(@ModelAttribute @Valid Pub newPub, Errors errors, Model model){
        if (errors.hasErrors()){
            model.addAttribute("title", "Add Pub");
            model.addAttribute("pubs", pubRepository.findAll());
            //model.addAttribute("pubs", newPub);
            return "/pubs/add";
        }

        pubRepository.save(newPub);
        return "redirect:";
    }

    @GetMapping("view/{pubID}")
    public String viewPubInfo(@PathVariable Integer pubID,
                              Model model,
                              Principal principal) {

        if (pubID == null) {
            return "pubs/index";
        } else {
            Optional<Pub> result = pubRepository.findById(pubID);

            if (result.isEmpty()) {
                model.addAttribute("title", "Invalid Pub ID");
            } else {
                Pub pub = result.get();

                if (principal != null) {
                    User user = userRepository.findByUsername(principal.getName());
                    Boolean isFavorite = user.getFavoritePubs().contains(pub);
                    model.addAttribute("isFavorite", isFavorite);
                }

                model.addAttribute("pub", pub);
                model.addAttribute("title", "Pub: " + pub.getName());
                model.addAttribute("reviews", pubReviewRepository.findAllByPubId(pubID));
                model.addAttribute("brews", brewRepository.findAllByPubId(pubID));
                model.addAttribute("favoritesCount", pub.getPubFavoriteUser().size());
            }
        }
        return "pubs/view";
    }

    @GetMapping("addFavoritePub/{pubId}/")
    public String processAddFavoritePub(@PathVariable Integer pubId,
                                        Principal principal) {
        Optional<User> resultUser = Optional.ofNullable(userRepository.findByUsername(principal.getName()));
        Optional<Pub> resultPub = pubRepository.findById(pubId);

        if (pubId == null || resultPub.isEmpty()) {
            return "redirect:";
        } else if (principal.getName() == null || resultUser.isEmpty()) {
            return "redirect:/pubs/view/" + pubId;
        } else if (resultPub.isPresent() && resultUser.isPresent()) {
            Pub pub = resultPub.get();
            User user = resultUser.get();

            user.addFavoritePub(pub);
            pub.addPubFavoriteUser(user);

            userRepository.save(user);
            pubRepository.save(pub);

            return "redirect:/pubs/view/" + pubId;
        }
        return "redirect:";
    }

    @GetMapping("removeFavoritePub/{pubId}/")
    public String processRemoveFavoritePub(@PathVariable Integer pubId,
                                           Principal principal) {
        Optional<User> resultUser = Optional.ofNullable(userRepository.findByUsername(principal.getName()));
        Optional<Pub> resultPub = pubRepository.findById(pubId);

        if (pubId == null || resultPub.isEmpty()) {
            return "redirect:";
        } else if (principal.getName() == null || resultUser.isEmpty()) {
            return "redirect:/pubs/view/" + pubId;
        } else if (resultPub.isPresent() && resultUser.isPresent()) {
            Pub pub = resultPub.get();
            User user = resultUser.get();

            user.removeFavoritePub(pub);
            pub.removePubFavoriteUser(user);

            userRepository.save(user);
            pubRepository.save(pub);

            return "redirect:/pubs/view/" + pubId;
        }
        return "redirect:";
    }


}
