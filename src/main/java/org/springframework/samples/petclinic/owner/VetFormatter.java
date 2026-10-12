/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.format.Formatter;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

/**
 * Instructs Spring MVC on how to parse and print elements of type 'Vet'. A vet is printed
 * as its id; a blank text is parsed as no vet.
 */
@Component
public class VetFormatter implements Formatter<Vet> {

	private final VetRepository vets;

	public VetFormatter(VetRepository vets) {
		this.vets = vets;
	}

	@Override
	public String print(Vet vet, Locale locale) {
		Integer id = vet.getId();
		return id != null ? id.toString() : "";
	}

	@Override
	public Vet parse(String text, Locale locale) throws ParseException {
		if (text == null || text.isBlank()) {
			return null;
		}
		int id;
		try {
			id = Integer.parseInt(text.trim());
		}
		catch (NumberFormatException ex) {
			throw new ParseException("vet id is not a number: " + text, 0);
		}
		for (Vet vet : this.vets.findAll()) {
			if (Objects.equals(vet.getId(), id)) {
				return vet;
			}
		}
		throw new ParseException("vet not found: " + text, 0);
	}

}
