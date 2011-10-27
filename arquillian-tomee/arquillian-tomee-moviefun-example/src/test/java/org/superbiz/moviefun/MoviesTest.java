package org.superbiz.moviefun;

import java.util.List;

import javax.ejb.embeddable.EJBContainer;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MoviesTest {

	private MoviesRemote movies;

	@Before
	public void setUp() throws Exception {
        EJBContainer ejbContainer = EJBContainer.createEJBContainer();
        Object object = ejbContainer.getContext().lookup("java:global/arquillian-tomee-moviefun-example/Movies!org.superbiz.moviefun.MoviesRemote");

        assertTrue(object instanceof MoviesRemote);
        movies = (MoviesRemote) object;
    }
	
	@Test
	public void testShouldAddAMovie() throws Exception {
		Movie movie = new Movie();
		movie.setDirector("Michael Bay");
		movie.setGenre("Action");
		movie.setRating(9);
		movie.setTitle("Bad Boys");
		movie.setYear(1995);
		movies.addMovie(movie);
		
		assertEquals(1, movies.count());
		List<Movie> moviesFound = movies.findByTitle("Bad Boys");
		
		assertEquals(1, moviesFound.size());
		assertEquals("Michael Bay", moviesFound.get(0).getDirector());
		assertEquals("Action", moviesFound.get(0).getGenre());
		assertEquals(9, moviesFound.get(0).getRating());
		assertEquals("Bad Boys", moviesFound.get(0).getTitle());
		assertEquals(1995, moviesFound.get(0).getYear());
	}
	
}
