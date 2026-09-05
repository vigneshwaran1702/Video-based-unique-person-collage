-- Supabase SQL Schema for Video-based Unique-Person Collage
-- Run this in your Supabase Project -> SQL Editor

-- 1. Create Collages Table
CREATE TABLE IF NOT EXISTS public.collages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    title TEXT NOT NULL DEFAULT 'Unique Person Collage',
    video_name TEXT,
    collage_style TEXT DEFAULT 'DYNAMIC_GRID',
    unique_persons_count INTEGER DEFAULT 0,
    fps_rate NUMERIC,
    clustering_threshold NUMERIC,
    image_url TEXT,
    storage_path TEXT,
    image_data_url TEXT,
    persons_data JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.collages ENABLE ROW LEVEL SECURITY;

-- Create Policies to allow users/guests to select, insert, and delete
CREATE POLICY "Allow public select on collages"
    ON public.collages FOR SELECT
    USING (true);

CREATE POLICY "Allow public insert on collages"
    ON public.collages FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Allow public delete on collages"
    ON public.collages FOR DELETE
    USING (true);

-- 2. Create Storage Bucket for Collage Images (if not already created)
INSERT INTO storage.buckets (id, name, public)
VALUES ('collages', 'collages', true)
ON CONFLICT (id) DO NOTHING;

-- Storage bucket access policies
CREATE POLICY "Allow public storage upload on collages"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id = 'collages');

CREATE POLICY "Allow public storage select on collages"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'collages');

CREATE POLICY "Allow public storage delete on collages"
    ON storage.objects FOR DELETE
    USING (bucket_id = 'collages');
