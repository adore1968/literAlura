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
    private Integer opcion = 10;
    private Scanner scanner = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    private void leerLibro(Libro libro) {
        System.out.printf("""
                        ----- LIBRO -----
                        Titulo: %s
                        Autor: %s
                        Idioma: %s
                        Numero de descargas: %d
                        -------------------- \n
                        """,
                libro.getTitulo(),
                libro.getAutor().getNombre(),
                libro.getIdioma(),
                libro.getNumeroDeDescargas());
    }

    private void buscarLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        String nombreLibro = scanner.next();
        String json = consumoApi.obtenerLibros(URL + "?search=" + nombreLibro.replace(" ", "+"));
        List<DatosLibro> libros = conversor.obtenerDatos(json, Datos.class).resultados();
        Optional<DatosLibro> libroOptional = libros.stream()
                .filter(l -> l.titulo().toLowerCase().contains(nombreLibro.toLowerCase()))
                .findFirst();
        if (libroOptional.isPresent()) {
            var libro = new Libro(libroOptional.get());
            libroRepository.save(libro);
            leerLibro(libro);
        }
        System.out.println("El libro no ha podido ser encontrado");
    }

    private void listarLibros() {
        List<Libro> libros = libroRepository.findAll();
        libros.stream()
                .forEach(this::leerLibro);
    }


    private void leerAutor(Autor autor) {
        System.out.printf("""
                        Autor: %s
                        Fecha de nacimiento: %s
                        Fecha de fallecimiento: %s
                        """,
                autor.getNombre(),
                autor.getFechaDeNacimiento(),
                autor.getFechaDeFallecimiento());

        var libros = autor.getLibros().stream()
                .map(a -> a.getTitulo())
                .collect(Collectors.toList());
        System.out.println("Libros: " + libros + "\n");
    }

    private void listarAutores() {
        List<Autor> autores = autorRepository.findAll();
        autores.stream()
                .forEach(this::leerAutor);
    }

    private void listarAutoresPorAño() {
        System.out.println("Ingresa el año vivo de autor(es) que desea buscar");
        Integer año = scanner.nextInt();
        List<Autor> autores = autorRepository.findByFechaDeFallecimientoGreaterThan(año);
        autores.stream()
                .forEach(this::leerAutor);
    }

    private void listarLibrosPorIdioma() {
        System.out.println("""
                Ingrese el idioma para buscar los libros
                es - español
                en - ingles
                fr - frances
                pt - portugues
                """);
        String idioma = scanner.next();
        List<Libro> libros = libroRepository.findByIdioma(idioma);
        libros.stream()
                .forEach(this::leerLibro);
    }

    public void mostrarMenu() {
        while (opcion != 6) {
            System.out.println("""
                    Elija la opcion a traves de su numero:
                    1- buscar libro por titulo
                    2- listar libros registrados
                    3- listar autores registrados
                    4- listar autores vivos en un determinado año
                    5- listar libros por idioma
                    6- salir
                    """);
            opcion = scanner.nextInt();
            if (opcion == 1) {
                buscarLibro();
            } else if (opcion == 2) {
                listarLibros();
            } else if (opcion == 3) {
                listarAutores();
            } else if (opcion == 4) {
                listarAutoresPorAño();
            } else if (opcion == 5) {
                listarLibrosPorIdioma();
            }
        }
    }
}
