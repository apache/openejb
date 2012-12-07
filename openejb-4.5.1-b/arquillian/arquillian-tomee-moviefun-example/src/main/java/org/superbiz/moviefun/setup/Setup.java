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
package org.superbiz.moviefun.setup;

import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.superbiz.moviefun.Movie;
import org.superbiz.moviefun.Movies;

public class Setup {
	
	@Inject @Examples private List<Movie> exampleMovies;
	@EJB private Movies moviesBean;
	
	public List<Movie> setup() {
		for (Movie movie : exampleMovies) {
			moviesBean.addMovie(movie);
		}
		
		return exampleMovies;
	}
}
