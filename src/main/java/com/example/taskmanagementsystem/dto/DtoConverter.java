package com.example.taskmanagementsystem.dto;

public interface DtoConverter<Entity, Dto, Request, Response>{
    Dto convertEntityToDto(Entity entity);
    Entity convertDtoToEntity(Dto dto);
    Dto convertRequestToDto(Request request);
    Response convertDtoToResponse(Dto dto);
}
