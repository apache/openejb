/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.client.RemoteInitialContextFactory;

public class EJBClient {

	public static void main(String[] args) {
		try {
			Properties p = new Properties();
			p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
			p.setProperty(Context.PROVIDER_URL, "http://localhost:8080/tomee/ejb");
			
			InitialContext context = new InitialContext(p);
			MoviesRemote movies = (MoviesRemote) context.lookup("MoviesRemote");
			List<Movie> allMovies = movies.getMovies();
			for (Movie movie : allMovies) {
				System.out.println(movie.getId() + ": " + movie.getTitle() + ", directed by: " + movie.getDirector() + ", year: " + movie.getYear() + ", genre: " + movie.getGenre() + ", rating: " + movie.getRating());
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		
	}

}
