package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final Environment env;
	private final OrderServiceClient orderServiceClient;
	// private final RestTemplate restTemplate;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);

		if (user == null)
			throw new UsernameNotFoundException(username + ": not found");

		return new org.springframework.security.core.userdetails.User(
			user.getEmail(),
			user.getEncryptedPassword(),
			true, true, true, true,
			new ArrayList<>());
	}

	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		User user = mapper.map(userDto, User.class);
		user.setEncryptedPassword(passwordEncoder.encode(userDto.getPassword()));

		userRepository.save(user);

		return mapper.map(user, UserDto.class);
	}

	@Override
	public UserDto getUserById(String userId) {
		User user = userRepository.findByUserId(userId);
		if (user == null) {
			throw new UsernameNotFoundException("user not found");
		}

		UserDto userDto = new ModelMapper().map(user, UserDto.class);

		/* Using as rest template */
		// String orderUrl = String.format(env.getProperty("order_service.url"), userId);
		// ResponseEntity<List<ResponseOrder>> orderListResponse =
		// 	restTemplate.exchange(
		// 		orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
		// 		});
		// userDto.setOrders(orderListResponse.getBody());

		/* Using a feign client */
		/* Feign exception handling */
		// List<ResponseOrder> orders = null;
		// try {
		// 	orders = orderServiceClient.getOrders(userId);
		// } catch (FeignException e) {
		// 	log.error(e.getMessage());
		// }

		List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
		userDto.setOrders(orders);

		return userDto;
	}

	@Override
	public Iterable<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null)
			throw new UsernameNotFoundException(email);

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		return mapper.map(user, UserDto.class);
	}
}
