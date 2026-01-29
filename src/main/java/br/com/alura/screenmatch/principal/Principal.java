package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    private static final String ENDERECO = "https://www.omdbapi.com/?t=";
    private static final String API_KEY = "&apikey=fb4120e1";

    public void exibeMenu() {
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

        // Exibe todas as temporadas completas
        temporadas.forEach(System.out::println);

        // --- Forma tradicional com for aninhado ---
        for (int i = 0; i < dados.totalTemporadas(); i++) {
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }

        // --- Forma usando lambdas e forEach ---
        temporadas.forEach(t ->
                t.episodios().forEach(e -> System.out.println(e.titulo()))
        );

        // Cria lista de Episodio (com temporada + dados do episódio)
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

//        // Busca por trecho no título
//        System.out.println("Digite um trecho do título do episódio");
//        var trechoTitulo = leitura.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())) // corrigido
//                .findFirst();
//
//        if (episodioBuscado.isPresent()) {
//            System.out.println("Episódio encontrado!");
//            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//        } else {
//            System.out.println("Episódio não encontrado!");
//        }

        // --- Filtro por ano de lançamento ---
        // System.out.println("A partir de que ano você deseja ver os episódios? ");
        // var ano = leitura.nextInt();
        // leitura.nextLine();
        //
        // LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        // DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        //
        // episodios.stream()
        //         .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
        //         .forEach(e -> System.out.println(
        //                 "Temporada: " + e.getTemporada() +
        //                         " Episódio: " + e.getTitulo() +
        //                         " Data lançamento: " + e.getDataLancamento().format(formatador)
        //         ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());
    }
}
