package com.alura.literAlura.principal;

import com.alura.literAlura.model.*;
import com.alura.literAlura.repository.AutorRepository;
import com.alura.literAlura.repository.LibroRepository;
import com.alura.literAlura.service.ConsumoApi;
import com.alura.literAlura.service.Conversor;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL = "https://gutendex.com/books/";
    private ConsumoApi consumoApi = new ConsumoApi();
    private Conversor conversor = new Conversor();
    private Integer opcion = 6;
    private Scanner scanner = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    private DatosLibro getLibro(String nombreLibro) {
        String json = consumoApi.obtenerLibros(URL + "?search=" + nombreLibro.replace(" ", "+"));
        List<DatosLibro> libros = conversor.obtenerDatos(json, Datos.class).resultados();
        Optional<DatosLibro> libro = libros.stream()
                .filter(l -> l.titulo().toLowerCase().contains(nombreLibro.toLowerCase()))
                .findFirst();
        if (libro.isPresent()) {
            return libro.get();
        }
        System.out.println("El libro no ha sido encontrado");
        return null;
    }

    private void leerLibro(Libro libro) {
        System.out.println("----- LIBRO -----");
        System.out.println("Titulo: " + libro.getTitulo());
        System.out.println("Autor: " + libro.getAutor().getNombre());
        System.out.println("Idioma: " + libro.getIdioma());
        System.out.println("Numero de descargas: " + libro.getNumeroDeDescargas());
        System.out.println("----------\n");
    }

    private void leerAutor(Autor autor) {
        System.out.println("Autor: " + autor.getNombre());
        System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
        System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
        List<String> libros = autor.getLibros().stream()
                .map(l -> l.getTitulo())
                .collect(Collectors.toList());
        System.out.println("Libros: " + libros + "\n");
    }

    public void mostrarMenu() {
        while (opcion != 0) {
            System.out.println("""
                    Elija la opcion a traves de su numero:
                    1- buscar libro por titulo
                    2- listar libros registrados
                    3- listar autores registrados
                    4- listar autores vivos en un determinado año
                    5- listar libros por idioma
                    0- salir
                    """);
            opcion = scanner.nextInt();
            if (opcion == 1) {
                System.out.println("Ingrese el nombre del libro que desea buscar:");
                String nombreLibro = scanner.next();
                Libro libro = new Libro(getLibro(nombreLibro));
                leerLibro(libro);
                libroRepository.save(libro);
            } else if (opcion == 2) {
                List<Libro> libros = libroRepository.findAll();
                libros.stream()
                        .forEach(this::leerLibro);
            } else if (opcion == 3) {
                List<Autor> autores = autorRepository.findAll();
                autores.stream()
                        .forEach(this::leerAutor);
            } else if (opcion == 4) {
                System.out.println("Ingresa el año vivo de autor(es) que desea buscar");
                Integer fechaDeFallecimiento = scanner.nextInt();
                List<Autor> autores = autorRepository.findByFechaDeFallecimientoGreaterThan(fechaDeFallecimiento);
                autores.stream()
                        .forEach(this::leerAutor);
            } else if (opcion == 5) {
                System.out.println("Ingrese el idioma para buscar los libros:");
                System.out.println("es - español");
                System.out.println("en - ingles");
                System.out.println("fr - frances");
                System.out.println("pt - portugues");
                String idioma = scanner.next();
                List<Libro> libros = libroRepository.findByIdioma(idioma);
                libros.stream()
                        .forEach(this::leerLibro);
            }
        }
    }
}
