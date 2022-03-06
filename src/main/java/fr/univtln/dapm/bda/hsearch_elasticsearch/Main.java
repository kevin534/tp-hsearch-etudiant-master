package fr.univtln.dapm.bda.hsearch_elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Author;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Book;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Genre;
import fr.univtln.dapm.bda.hsearch_elasticsearch.search.IndexSearchApi;

public class Main {
	public static void main(String[] args) throws IOException {

		// Instanciation de notre classe IndexSearchApi pour indexer et rechercher
		IndexSearchApi api = new IndexSearchApi();

		// Réindexation à chaque nouvel appel de la classe Main (à commenter si besoin).
		api.purgeIndex();
		api.indexFilesInFolder("C:\\Users\\Kevin Toukam\\tp-hsearch-etudiant-master\\src\\main\\resources\\data\\raw");

		// Recherche.
		System.out.println("Recherche de 'pilgrime tale' dans le titre : "
				+ api.searchInTitle("pilgrimge tale"));

		// Recherche de mot-clé dans le contenu des livres.
		System.out.println("Recherche du mot-clé 'Produced' dans le content : "
				+ api.searchInContent("Produced"));
		//#2. méthode permettant d'effectuer une recherche exacte (phrase)
		System.out.println("Recherche exacte de  'Produced by John Bickers, and Dagny' dans le content : "
				+ api.searchWordExactInContent("Produced by John Bickers, and Dagny"));
		//#3. Ecrire une méthode à deux paramètres q1 et q2 permettant d'effectuer une recherche exacte de q1 dans le titre
		//et une recherche approximative de q2 dans le contenu.
		System.out.println("Recherche exacte d'un titre et d'un content : "
				+ api.searchWordExactTitleAndContent("Allan and the Holy Flower 5174.txt","Produced by John Bickers, and Dagny"));

		Genre genre = new Genre();
		genre.setType("comique");
		genre.setDescription("je vous le conseille");
		Author author = new Author();
		author.setFirstName("H. Rider Haggard");
		author.setLastName("kevin");
		author.setBiography("grand de taille et avec un teint noir ");
		Book book = new Book();
		book.setId(1);
		book.setContent("Produced by John Bickers, and Dagny");
		book.setTitle("Allan and the Holy Flower 5174.txt");
		List<Book> list = new ArrayList<>();
		list.add(book);
		author.setListBook(list);

		//Ajouter une fonctionnalité permettant de filtrer la recherche d'un livre sur un auteur
		System.out.println("Le livre de l'auteur H. Rider Haggard est : "
				+ api.searchInAuthorOfBook("H. Rider", " Haggard"));
		// 5. Ajouter une fonctionnalité permettant de filtrer la recherche d'un livre sur un genre particulier
		System.out.println("Le livre du genre H. Rider Haggard est : "
				+ api.searchInGenreOfBook("H. Rider", " Haggard"));
		System.exit(0);
	}
}
