package com.selloLegitimo.MockJurados.util;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class GeneradorDeterministico {

    private static final String[] NOMBRES = {
        "Carlos", "Maria", "Jose", "Ana", "Luis", "Carmen", "Juan", "Martha",
        "Pedro", "Sofia", "Diego", "Laura", "Andres", "Paula", "Felipe", "Diana",
        "Santiago", "Valentina", "Camilo", "Daniela", "Esteban", "Alejandra",
        "Ricardo", "Manuela", "Oscar", "Natalia", "Fernando", "Monica", "Hector", "Rosa"
    };

    private static final String[] APELLIDOS = {
        "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez", "Sanchez",
        "Ramirez", "Torres", "Flores", "Rivera", "Gomez", "Diaz", "Moreno", "Jimenez",
        "Mendoza", "Ortiz", "Cardenas", "Rojas", "Castillo", "Vargas", "Reyes",
        "Hernandez", "Alvarez", "Romero", "Contreras", "Medina", "Silva", "Pardo", "Arias"
    };

    private static final String[] DEPARTAMENTOS = {
        "Cundinamarca", "Antioquia", "Valle del Cauca", "Atlantico", "Santander", "Boyaca"
    };

    private static final String[][] MUNICIPIOS = {
        {"Bogota", "Soacha", "Chia", "Zipaquirá", "Facatativa"},
        {"Medellin", "Envigado", "Itagui", "Bello", "Rionegro"},
        {"Cali", "Buenaventura", "Palmira", "Tulua", "Yumbo"},
        {"Barranquilla", "Soledad", "Malambo", "Puerto Colombia", "Sabanagrande"},
        {"Bucaramanga", "Floridablanca", "Giron", "Piedecuesta", "San Gil"},
        {"Tunja", "Duitama", "Sogamoso", "Chiquinquira", "Paipa"}
    };

    public Random crearRandom(long seed, String eleccionId, String departamento, String municipio) {
        long hash = (seed * 31 + eleccionId.hashCode()) * 31 + departamento.hashCode();
        hash = hash * 31 + municipio.hashCode();
        return new Random(hash);
    }

    public String generarNombre(Random rng) {
        return NOMBRES[rng.nextInt(NOMBRES.length)];
    }

    public String generarApellido(Random rng) {
        return APELLIDOS[rng.nextInt(APELLIDOS.length)];
    }

    public String generarCedula(Random rng) {
        long num = 1_000_000L + (long)(rng.nextDouble() * 90_000_000L);
        return String.valueOf(num);
    }

    public int indiceDepartamento(Random rng) {
        return rng.nextInt(DEPARTAMENTOS.length);
    }

    public String departamentoPorIndice(int idx) {
        return DEPARTAMENTOS[idx];
    }

    public String municipioPorIndice(int deptIdx, Random rng) {
        String[] municipios = MUNICIPIOS[deptIdx];
        return municipios[rng.nextInt(municipios.length)];
    }
}
