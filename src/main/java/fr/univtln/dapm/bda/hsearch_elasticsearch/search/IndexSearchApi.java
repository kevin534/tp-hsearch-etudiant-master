package fr.univtln.dapm.bda.hsearch_elasticsearch.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Author;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Genre;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;

import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Book;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.BookResult;

/**
 * API pour l'indexation et la recherche de documents (des livres ici).
 * 
 * @author vincent
 *
 */
public class IndexSearchApi {
	private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("bda");
	private EntityManager entityManager = entityManagerFactory.createEntityManager();
	private SearchSession fullTextSession = Search.session(entityManager);

	public void purgeIndex() {
		Search.mapping(entityManagerFactory).scope(Book.class).workspace().purge();
		fullTextSession.indexingPlan().execute();
	}

	public boolean indexFilesInFolder(String folderPath) throws IOException {
		entityManager.getTransaction().begin();
		Files.list(Paths.get(folderPath)).filter(Files::isRegularFile).forEach(t -> {
			try {
				indexFile(t);
			} catch (IOException e) {
				System.err.println("Cannot process " + t.toString());
			}
		});
		entityManager.getTransaction().commit();
		return true;
	}

	public boolean indexFile(Path path) throws IOException {
		String fileName = path.getFileName().toString();
		String fileContent = new String(Files.readAllBytes(path));

		Book book = new Book();
		book.setTitle(fileName);
		book.setContent(fileContent);
		entityManager.persist(book); // le livre est automatiquement ajouté à la base de données et indexé!

		/*Author author = new Author();
		author.setFirstName("Toukam");
		author.setLastName("kevin");
		book.getAuthor();
		author.getListBook().add(book);

		entityManager.persist(author);
*/


		return true;
	}

	public List<BookResult> searchInTitle(String query) {
		List<BookResult> bookResults = new ArrayList<>();

		List<List<?>> results = fullTextSession.search(Book.class).select(f -> f.composite(f.score(), f.entity()))
				.where(f -> f.match().fields("title").matching(query)).fetchAllHits();

		for (List<?> result : results) {
			float score = (float) result.get(0);
			Book book = (Book) result.get(1);
			bookResults.add(new BookResult(book, score));
		}

		return bookResults;
	}
	//#1. Recherche de mots-clés dans le contenu des livres.
	public List<BookResult> searchInContent(String query) {
		List<BookResult> bookResults = new ArrayList<>();

		List<List<?>> results = fullTextSession.search(Book.class).select(f -> f.composite(f.score(), f.entity()))
				.where(f -> f.match().fields("content").matching(query)).fetchAllHits();

		for (List<?> result : results) {
			float score = (float) result.get(0);
			Book book = (Book) result.get(1);
			bookResults.add(new BookResult(book, score));
		}

		return bookResults;
	}
	//#2. méthode permettant d'effectuer une recherche exacte (phrase)
	public List<BookResult> searchWordExactInContent(String query) {
		List<BookResult> bookResults = new ArrayList<>();

		List<List<?>> results = fullTextSession.search(Book.class).select(f -> f.composite(f.score(), f.entity()))
				.where(f -> f.phrase().fields("content").matching(query)).fetchAllHits();
		for (List<?> result : results) {
			float score = (float) result.get(0);
			Book book = (Book) result.get(1);
			bookResults.add(new BookResult(book, score));
		}
		return bookResults;
	}
	//#3. Ecrire une méthode à deux paramètres q1 et q2 permettant d'effectuer une recherche exacte de q1 dans le titre
	//et une recherche approximative de q2 dans le contenu.
	public List<BookResult> searchWordExactTitleAndContent(String q1 ,String q2) {
		List<BookResult> bookResults = new ArrayList<>();

		List<List<?>> results = fullTextSession.search(Book.class).select(f -> f.composite(f.score(), f.entity()))
		  .where( f -> f.bool()
				.should( f.phrase().field( "title" )
						.matching( q1 ) )
				.should( f.phrase().field( "content" )
						.matching( q2 ) )
		).fetchAllHits();

		for (List<?> result : results) {
			float score = (float) result.get(0);
			Book book = (Book) result.get(1);
			bookResults.add(new BookResult(book, score));
		}
		return bookResults;
	}
	//Ajouter une fonctionnalité permettant de filtrer la recherche d'un livre sur un auteur
	public List<BookResult> searchInAuthorOfBook(String query) {
		List<BookResult> bookResults = new ArrayList<>();

		List<List<?>> results = fullTextSession.search(Book.class).select(f -> f.composite(f.score(), f.entity()))
				.where(f -> f.match().field("author.firstname").matching(query)).fetchAllHits();

		for (List<?> result : results) {
			float score = (float) result.get(0);
			Book book = (Book) result.get(1);
			bookResults.add(new BookResult(book, score));
		}

		return bookResults;
	}


}
