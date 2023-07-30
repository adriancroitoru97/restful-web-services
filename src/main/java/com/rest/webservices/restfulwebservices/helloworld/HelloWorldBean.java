package com.rest.webservices.restfulwebservices.helloworld;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class HelloWorldBean {
    private final String message;
}
