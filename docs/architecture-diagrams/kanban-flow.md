```mermaid
flowchart TD
    A[docs/user-stories/*.md] --> B[GitHub Issues]
    B --> C[GitHub Projects Kanban Board]
    C --> D[Portfolio Site Showcase]

    style A fill:#f9f,stroke:#333,stroke-width:1px,color:#000
    style B fill:#bbf,stroke:#333,stroke-width:1px,color:#000
    style C fill:#bfb,stroke:#333,stroke-width:1px,color:#000
    style D fill:#ffb,stroke:#333,stroke-width:1px,color:#000

    click A href "docs/user-stories/" "View User Stories"
    click C href "https://github.com/users/MichaelJGKopp/projects/1" "View Kanban Board"
