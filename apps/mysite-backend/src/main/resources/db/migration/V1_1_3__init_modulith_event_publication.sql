-- Spring Modulith Event Publication Table
-- This table is used by Spring Modulith to track event publication for event externalization
-- See: https://docs.spring.io/spring-modulith/reference/events.html#event-externalization

CREATE TABLE IF NOT EXISTS public.event_publication (
    id UUID NOT NULL,
    listener_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMPTZ NOT NULL,
    completion_date TIMESTAMPTZ,
    PRIMARY KEY (id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_event_publication_listener ON public.event_publication (listener_id);

CREATE INDEX IF NOT EXISTS idx_event_publication_event_type ON public.event_publication (event_type);

CREATE INDEX IF NOT EXISTS idx_event_publication_completion_date ON public.event_publication (completion_date);