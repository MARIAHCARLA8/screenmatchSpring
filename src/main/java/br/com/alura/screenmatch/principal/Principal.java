package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=fb4120e1";

    public void exibMenu() {
        System.out.println("Digite o nome da série para a busca: ");
        var nomeSerie = leitura.nextLine();

        // Codifica o nome da série para evitar caracteres inválidos na URL
        var encodedNomeSerie = URLEncoder.encode(nomeSerie, StandardCharsets.UTF_8);

        // Busca os dados da série
        var json = consumo.obterDados(ENDERECO + encodedNomeSerie + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        // Lista para armazenar as temporadas
        List<DadosTemporada> temporadas = new ArrayList<>();

        // Busca cada temporada da série
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            var jsonTemporada = consumo.obterDados(
                    ENDERECO + encodedNomeSerie + "&season=" + i + API_KEY
            );
            DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        // Exibe todas as temporadas
        temporadas.forEach(System.out::println);
    }
}
