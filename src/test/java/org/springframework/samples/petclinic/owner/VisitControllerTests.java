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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(value = VisitController.class,
		includeFilters = @ComponentScan.Filter(value = VetFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private VetRepository vets;

	@BeforeEach
	void init() {
		Vet vet = new Vet();
		vet.setId(7);
		vet.setFirstName("Helen");
		vet.setLastName("Leary");
		given(this.vets.findAll()).willReturn(List.of(vet));

		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void initNewVisitForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void processNewVisitFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("date", LocalDate.now().plusDays(1).toString())
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void processNewVisitFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("name",
					"George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void processNewVisitFormHasErrorsWhenVisitDateIsNotInFuture() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("date", LocalDate.now().toString())
				.param("description", "Visit Description"))
			.andExpect(model().attributeHasFieldErrors("visit", "date"))
			.andExpect(model().attributeHasFieldErrorCode("visit", "date", "typeMismatch.visitDate"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void processNewVisitFormHasErrorsWhenDescriptionIsTooLong() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("date", LocalDate.now().plusDays(1).toString())
				.param("description", "a".repeat(256)))
			.andExpect(model().attributeHasFieldErrors("visit", "description"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	private Visit savedVisit() {
		ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
		verify(this.owners).save(captor.capture());
		return captor.getValue().getPet(TEST_PET_ID).getVisits().iterator().next();
	}

	private static String tomorrow() {
		return LocalDate.now().plusDays(1).toString();
	}

	@Test
	void initNewVisitFormListsVetsWithEmptyOption() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("vets"))
			.andExpect(content().string(containsString("<option value=\"\" selected=\"selected\">")))
			.andExpect(content().string(containsString("<option value=\"7\">Helen Leary</option>")));
	}

	@Test
	void processNewVisitFormSavesChosenVet() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet", "7"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
		Vet vet = savedVisit().getVet();
		assertThat(vet).isNotNull();
		assertThat(vet.getId()).isEqualTo(7);
	}

	@Test
	void processNewVisitFormWithBlankVetSavesNoVet() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet", ""))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
		assertThat(savedVisit().getVet()).isNull();
	}

	@Test
	void processNewVisitFormWithoutVetFieldSavesNoVet() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
		assertThat(savedVisit().getVet()).isNull();
	}

	@Test
	void invalidVetIdRendersFriendlyMessageNotRawConversionError() throws Exception {
		for (String badVet : List.of("9999", "abc")) {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("date", tomorrow())
					.param("description", "Visit Description")
					.param("vet", badVet))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdateVisitForm"))
				.andExpect(content().string(containsString("invalid vet")))
				.andExpect(content().string(not(containsString("Failed to convert"))));
		}
	}

	@Test
	void processNewVisitFormRejectsUnknownVetId() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet", "9999"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("visit", "vet"))
			.andExpect(model().attributeHasFieldErrorCode("visit", "vet", "typeMismatch"))
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
		verify(this.owners, never()).save(any(Owner.class));
	}

	@Test
	void processNewVisitFormRejectsNonNumericVetId() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet", "abc"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("visit", "vet"))
			.andExpect(model().attributeHasFieldErrorCode("visit", "vet", "typeMismatch"))
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
		verify(this.owners, never()).save(any(Owner.class));
	}

	@Test
	void processNewVisitFormWithErrorsKeepsVetsAndSelection() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "")
				.param("vet", "7"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("visit", "description"))
			.andExpect(model().attributeExists("vets"))
			.andExpect(view().name("pets/createOrUpdateVisitForm"))
			.andExpect(
					content().string(containsString("<option value=\"7\" selected=\"selected\">Helen Leary</option>")));
	}

	@Test
	void processNewVisitFormIgnoresVetIdParameter() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet.id", "7"))
			.andExpect(status().is3xxRedirection());
		assertThat(savedVisit().getVet()).isNull();
	}

	@Test
	void processNewVisitFormDoesNotBindNestedVetProperties() throws Exception {
		Vet sharedVet = this.vets.findAll().iterator().next();
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet", "7")
				.param("vet.firstName", "Hacked")
				.param("vet.lastName", "Hacked"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
		assertThat(sharedVet.getFirstName()).isEqualTo("Helen");
		assertThat(sharedVet.getLastName()).isEqualTo("Leary");
		assertThat(savedVisit().getVet()).isSameAs(sharedVet);
	}

	@Test
	void processNewVisitFormWithNestedVetPropertyButNoVetDoesNotFail() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description")
				.param("vet.firstName", "Hacked"))
			.andExpect(status().is3xxRedirection());
		assertThat(savedVisit().getVet()).isNull();
	}

	@Test
	void formWithNoVetsOffersOnlyEmptyOptionAndSaves() throws Exception {
		given(this.vets.findAll()).willReturn(List.of());
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("<option value=\"\" selected=\"selected\">")))
			.andExpect(content().string(not(containsString("Leary"))));
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", tomorrow())
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection());
	}

}
