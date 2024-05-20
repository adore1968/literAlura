package com.alura.literAlura.service;

public interface IConversor {
    <T> T obtenerDatos(String json, Class<T> clase);
}
