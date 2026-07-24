-- Supabase PostgreSQL Schema for Loksewa Quizzes

CREATE TABLE IF NOT EXISTS public.loksewa_quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    questions JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.loksewa_quizzes ENABLE ROW LEVEL SECURITY;

-- Allow public read access to anon users
CREATE POLICY "Allow public read access to loksewa_quizzes"
    ON public.loksewa_quizzes
    FOR SELECT
    TO anon
    USING (true);
