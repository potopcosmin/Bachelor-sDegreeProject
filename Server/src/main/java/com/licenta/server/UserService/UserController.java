package com.licenta.server.UserService;

import com.licenta.server.CarRepair.CarService;
import com.licenta.server.CarRepair.DistanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository UserRepo;

    @GetMapping()
    public @ResponseBody List<Map<String,Object>> listall(Model model){
        List<User> allUsers=UserRepo.findAll();
        List<Map<String,Object>> response=new ArrayList<>();

        for (User user:allUsers
             ) {
            Map<String,Object> userDetails=retreiveUserInfo(user);
            response.add(userDetails);
            System.out.println(userDetails);
        }
        return response;
    }

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody User user){
            UserRepo.save(user);
            System.out.println(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User succesfully registered");
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> cred){
        String email=cred.keySet().stream().findFirst().get();
        String password=cred.get(email);
        User user =UserRepo.findByEmail(email);
        Map<String, Object> userInfo = retreiveUserInfo(user);
        if (user==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login Failed");
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).body(userInfo);
        }

    }

    private Map<String, Object> retreiveUserInfo(User user) {
        Map<String,Object> userInfo =new HashMap<>();
        userInfo.put("id",user.getId());
        userInfo.put("username",user.getUsername());
        userInfo.put("email",user.getEmail());
        userInfo.put("first_name",user.getFirst_name());
        userInfo.put("last_name",user.getLast_name());
        return userInfo;
    }


    @GetMapping("find/user")
    public ResponseEntity find(@RequestBody HashMap<String,String> details ,@RequestHeader("atribute") String atribute){
        String value=details.get("email");
        System.out.println(atribute);
        User user=null;
        if(atribute.equals("email")){
            user=UserRepo.findByEmail(value);
        }
        if(atribute.equals("username"))
        {
        user=UserRepo.findByEmailAndType(value,details.get("type"));
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Atribute value incorrect");
        }
        if(user==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.status(HttpStatus.FOUND).body(user);

    }

}
