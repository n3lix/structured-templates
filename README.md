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
  Use two additional variables inside File Templates:
    - `$FILE_NAME_CAMEL` – user input in camelCase
    - `$FILE_NAME_PASCAL` – user input in PascalCase

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

---

## Getting Started

### 1. Open the Template Manager
Go to **File → Settings → Tools → Structure Templates**  
(or **Preferences** on macOS).

### 2. Create a Structure Template
Add folders and files, choose File Templates for file entries, and build any hierarchy you need.

### 3. Use Custom Variables
Inside IntelliJ File Templates, you can use:
- `$FILE_NAME_CAMEL`
- `$FILE_NAME_PASCAL`

These expand based on the name you enter when generating the structure.

### 4. Generate a Structure
Right‑click a folder in the Project tool window →  
**New → Create From Structure Template** → choose a template → enter a name.

### 5. Import/Export Templates
Use the import/export options to share templates with your team.  
Exports include both structure templates and the File Templates they depend on.
