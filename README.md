# Structured Templates

Structured Templates is an IntelliJ IDEA plugin that lets you define reusable folder and file hierarchies and generate them anywhere in your project. Each file in a structure can be backed by an IntelliJ File Template, giving you full control over naming, content, and conventions.

This plugin is ideal for teams and developers who frequently create similar components, modules, services, or feature folders and want a fast, consistent way to scaffold them.

---

## Features

- **Reusable structured templates**  
  Define hierarchical folder/file layouts and generate them with a single action.

- **Backed by IntelliJ File Templates**  
  Each file in your structure can use any IntelliJ File Template for fully customizable content.

- **Custom variables for smarter generation**  
  Use three additional variables inside File Templates:
    - `${FILE_NAME_CAMEL}` – user input in camelCase
    - `${FILE_NAME_PASCAL}` – user input in PascalCase
    - `${FILE_NAME_KEBAB}` – user input in kebab-case

- **Generate from the New menu**  
  Right‑click any folder → **New → Create From Structured Template** → pick a template → done.

- **Import and export template packs**  
  Export all your structured templates and their associated File Templates into a single XML file.  
  No need to distribute File Templates separately — everything is bundled together.

- **Nested folders and complex hierarchies**  
  Build multi‑level structures with unlimited depth.

- **Dynamic template dropdown**  
  Templates appear automatically in a clean, organized menu, with optional custom icons.

- **Project‑level storage**  
  Templates are stored per project, making it easy to maintain different setups across codebases.