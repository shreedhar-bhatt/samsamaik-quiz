import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { DOMParser } from "https://deno.land/x/deno_dom@v0.1.38/deno-dom-wasm.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const GORKHAPATRA_URL = "https://gorkhapatraonline.com/categories/loksewa";

interface QuestionItem {
  id: number;
  question: string;
  options: string[];
  answer_index: number;
}

serve(async (req) => {
  try {
    const response = await fetch(GORKHAPATRA_URL, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Accept-Language": "ne-NP,ne;q=0.9,en;q=0.8",
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch Gorkhapatra page: ${response.statusText}`);
    }

    const htmlText = await response.text();
    const doc = new DOMParser().parseFromString(htmlText, "text/html");

    if (!doc) {
      throw new Error("Failed to parse HTML document");
    }

    const titleElement = doc.querySelector(".category-title, h1, .news-title");
    const quizTitle = titleElement?.textContent?.trim() || "लोकसेवा वस्तुगत प्रश्नोत्तर";

    const parsedQuestions: QuestionItem[] = [];

    const paragraphs = doc.querySelectorAll("p, .detail-content p");
    let qCounter = 1;
    let currentQuestion = "";
    let currentOptions: string[] = [];

    paragraphs.forEach((p) => {
      const text = p.textContent.trim();
      
      if (/^[०-९1-9]+\.\s*/.test(text)) {
        if (currentQuestion && currentOptions.length >= 2) {
          parsedQuestions.push({
            id: qCounter++,
            question: currentQuestion,
            options: currentOptions,
            answer_index: 0,
          });
        }
        currentQuestion = text;
        currentOptions = [];
      } else if (/^[क-घa-d][\.\)]\s*/.test(text)) {
        currentOptions.push(text);
      }
    });

    if (currentQuestion && currentOptions.length >= 2) {
      parsedQuestions.push({
        id: qCounter,
        question: currentQuestion,
        options: currentOptions,
        answer_index: 0,
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const { data, error } = await supabase
      .from("loksewa_quizzes")
      .insert([
        {
          title: quizTitle,
          questions: parsedQuestions,
        },
      ])
      .select();

    if (error) throw error;

    return new Response(
      JSON.stringify({ success: true, count: parsedQuestions.length, data }),
      { headers: { "Content-Type": "application/json; charset=utf-8" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ success: false, error: err.message }),
      { status: 500, headers: { "Content-Type": "application/json; charset=utf-8" } }
    );
  }
});
