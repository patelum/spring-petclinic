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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

/**
 * Test class for {@link VetFormatter}
 */
@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage
class VetFormatterTests {

	@Mock
	private VetRepository vets;

	private VetFormatter vetFormatter;

	@BeforeEach
	void setup() {
		this.vetFormatter = new VetFormatter(vets);
	}

	@Test
	void shouldPrintVetId() {
		assertThat(vetFormatter.print(makeVet(7, "Helen", "Leary"), Locale.ENGLISH)).isEqualTo("7");
	}

	@Test
	void shouldPrintEmptyStringForVetWithoutId() {
		assertThat(vetFormatter.print(new Vet(), Locale.ENGLISH)).isEmpty();
	}

	@Test
	void shouldParseKnownId() throws ParseException {
		given(vets.findAll()).willReturn(List.of(makeVet(1, "James", "Carter"), makeVet(2, "Helen", "Leary")));
		Vet vet = vetFormatter.parse("2", Locale.ENGLISH);
		assertThat(vet).isNotNull();
		assertThat(vet.getId()).isEqualTo(2);
		assertThat(vet.getLastName()).isEqualTo("Leary");
	}

	@Test
	void shouldParseBlankAsNull() throws ParseException {
		assertThat(vetFormatter.parse("", Locale.ENGLISH)).isNull();
		assertThat(vetFormatter.parse("   ", Locale.ENGLISH)).isNull();
		assertThat(vetFormatter.parse(null, Locale.ENGLISH)).isNull();
	}

	@Test
	void shouldThrowParseExceptionForUnknownId() {
		given(vets.findAll()).willReturn(List.of(makeVet(1, "James", "Carter")));
		assertThatThrownBy(() -> vetFormatter.parse("9999", Locale.ENGLISH)).isInstanceOf(ParseException.class);
	}

	@Test
	void shouldThrowParseExceptionForNonNumericId() {
		assertThatThrownBy(() -> vetFormatter.parse("abc", Locale.ENGLISH)).isInstanceOf(ParseException.class);
	}

	private Vet makeVet(int id, String firstName, String lastName) {
		Vet vet = new Vet();
		vet.setId(id);
		vet.setFirstName(firstName);
		vet.setLastName(lastName);
		return vet;
	}

}
