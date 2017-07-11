package com.hackovation.hybo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceImpl implements UsersService{
	
	private UsersRepository usersRepository;
	
	 @Autowired
	 public void setUsersRepository(UsersRepository usersRepository) {
	        this.usersRepository = usersRepository;
	    }
	
	@Override
    public Iterable<Users> listAllUsers() {
		System.out.println("challllllllllllllllllllllllllaaaaaaaaaa");
         Iterable<Users> itr = usersRepository.findAll();
        itr.forEach(s -> System.out.println(s));
        return itr;
    }
	

}
