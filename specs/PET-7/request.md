PET-7: Duplicate pets can be created by adding a trailing space.
Steps: open an owner who already has a pet called Leo, click Add New Pet, enter the name 'Leo ' (with a trailing space), any birth date and type, save.
Expected: the form rejects it with 'already exists', like it does for 'Leo' or 'leo'.
Actual: the pet is saved and the owner now shows two pets called Leo. The same happens with a leading space, and when editing a pet's name.
