import { createClient } from '@supabase/supabase-js';

// Read credentials from Vite / Next environment variables
const supabaseUrl =
  import.meta.env.VITE_SUPABASE_URL ||
  import.meta.env.NEXT_PUBLIC_SUPABASE_URL ||
  'https://axkfyqvwgdlptgvbonut.supabase.co';

const supabaseKey =
  import.meta.env.VITE_SUPABASE_ANON_KEY ||
  import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY ||
  import.meta.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY ||
  'sb_publishable_9jZwoM-XQbBS2VL8_3fsbQ_2xuPMgMg';

export const supabase = createClient(supabaseUrl, supabaseKey, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true
  }
});

/**
 * Check if connection to Supabase is active
 */
export async function testSupabaseConnection() {
  try {
    if (!supabaseUrl || !supabaseKey) {
      return { connected: false, error: 'Supabase credentials missing' };
    }
    // Test auth session or lightweight probe
    const { data, error } = await supabase.auth.getSession();
    if (error && error.message && !error.message.includes('Auth session missing')) {
      return { connected: false, error: error.message };
    }
    return { connected: true, session: data?.session };
  } catch (err) {
    return { connected: false, error: err.message };
  }
}

/**
 * Auth Helpers
 */
export async function getSessionUser() {
  const { data: { session } } = await supabase.auth.getSession();
  return session?.user || null;
}

export async function signInUser(email, password) {
  const { data, error } = await supabase.auth.signInWithPassword({ email, password });
  if (error) throw error;
  return data.user;
}

export async function signUpUser(email, password) {
  const { data, error } = await supabase.auth.signUp({ email, password });
  if (error) throw error;
  return data.user;
}

export async function signInGuest() {
  const { data, error } = await supabase.auth.signInAnonymously();
  if (error) throw error;
  return data.user;
}

export async function signOutUser() {
  const { error } = await supabase.auth.signOut();
  if (error) throw error;
}

/**
 * Upload Base64 Data URL to Supabase Storage Bucket 'collages'
 */
async function uploadImageToStorage(dataUrl, fileName) {
  try {
    // Convert data URL to Blob
    const response = await fetch(dataUrl);
    const blob = await response.blob();
    const filePath = `uploads/${Date.now()}_${fileName}.png`;

    const { data, error } = await supabase.storage
      .from('collages')
      .upload(filePath, blob, {
        contentType: 'image/png',
        upsert: true
      });

    if (error) {
      console.warn('Storage upload note:', error.message);
      return null;
    }

    const { data: publicUrlData } = supabase.storage
      .from('collages')
      .getPublicUrl(filePath);

    return {
      storagePath: filePath,
      publicUrl: publicUrlData?.publicUrl || null
    };
  } catch (e) {
    console.warn('Failed to upload image blob:', e);
    return null;
  }
}

/**
 * Save collage result to Supabase
 */
export async function saveCollageToSupabase({
  title = 'Unique Person Collage',
  videoName = 'video.mp4',
  collageDataUrl,
  style = 'DYNAMIC_GRID',
  persons = [],
  fps = 2.5,
  clusteringThreshold = 0.68
}) {
  const user = await getSessionUser();
  const userId = user ? user.id : null;

  // Try uploading image to Supabase Storage
  const storageResult = await uploadImageToStorage(
    collageDataUrl,
    `collage_${Date.now()}`
  );

  const payload = {
    user_id: userId,
    title,
    video_name: videoName,
    collage_style: style,
    unique_persons_count: persons.length,
    fps_rate: fps,
    clustering_threshold: clusteringThreshold,
    image_url: storageResult?.publicUrl || null,
    storage_path: storageResult?.storagePath || null,
    // Store lightweight snapshot of detected persons metadata
    persons_data: persons.map(p => ({
      name: p.name,
      appearanceCount: p.appearanceCount,
      screenTime: p.screenTime,
      qualityScore: p.qualityScore,
      cropDataUrl: p.bestDetection?.cropDataUrl
    })),
    // Fallback if storage bucket isn't configured yet
    image_data_url: storageResult?.publicUrl ? null : collageDataUrl.slice(0, 1000000),
    created_at: new Date().toISOString()
  };

  const { data, error } = await supabase
    .from('collages')
    .insert([payload])
    .select()
    .single();

  if (error) {
    // If table doesn't exist yet or RLS error, provide clear guidance
    console.error('Supabase DB Insert Error:', error);
    throw error;
  }

  return data;
}

/**
 * Retrieve saved collages from Supabase
 */
export async function fetchSavedCollages() {
  const { data, error } = await supabase
    .from('collages')
    .select('*')
    .order('created_at', { ascending: false });

  if (error) {
    throw error;
  }
  return data || [];
}

/**
 * Delete a saved collage
 */
export async function deleteCollage(id, storagePath) {
  if (storagePath) {
    await supabase.storage.from('collages').remove([storagePath]).catch(console.warn);
  }
  const { error } = await supabase
    .from('collages')
    .delete()
    .eq('id', id);

  if (error) throw error;
  return true;
}
