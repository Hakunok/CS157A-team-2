import React from "react";
import { useNavigate, useLocation, useParams } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { Input } from "@/components/ui/input.jsx";
import { Label } from "@/components/ui/label.jsx";
import { KindBadge } from "@/components/ui/KindBadge.jsx"
import MinimalTiptapEditor from "@/components/editor/minimal-tiptap.jsx";
import LoadingOverlay from "@/components/shared/LoadingOverlay.jsx";
import { publicationApi } from "@/lib/api.js";
import { FileText, BookOpen, Newspaper, Save, Plus, ArrowLeft } from "lucide-react";
import { toast } from "sonner"

function useQueryParam(key) {
  const params = new URLSearchParams(useLocation().search);
  return params.get(key);
}

export default function DraftFormPage() {
  const navigate = useNavigate();
  const { pubId } = useParams();
  const isEditMode = Boolean(pubId);

  const kindFromQuery = useQueryParam("kind")?.toUpperCase();
  const [kind, setKind] = React.useState(kindFromQuery || "");
  const [loading, setLoading] = React.useState(isEditMode);
  const [title, setTitle] = React.useState("");
  const [doi, setDoi] = React.useState("");
  const [pdfUrl, setPdfUrl] = React.useState("");
  const [content, setContent] = React.useState("");

  const isBlogLike = kind === "BLOG" || kind === "ARTICLE";
  const isPaper = kind === "PAPER";

  React.useEffect(() => {
    if (!isEditMode) return;

    const loadDraft = async () => {
      try {
        const pub = await publicationApi.getById(pubId);
        setKind(pub.kind);
        setTitle(pub.title);
        setContent(pub.content || "");
        if (pub.kind === "PAPER") {
          setDoi(pub.doi || "");
          setPdfUrl(pub.url || "");
        }
      } catch {
        console.error("Failed to load draft");
      } finally {
        setLoading(false);
      }
    };

    loadDraft();
  }, [isEditMode, pubId]);

  if (!isEditMode && (!kind || (!isPaper && !isBlogLike))) {
    return (
        <div className="min-h-screen bg-background flex items-center justify-center px-6">
          <div className="max-w-md w-full text-center space-y-6">
            <h2 className="text-xl text-destructive font-semibold">
              Invalid Publication Type
            </h2>
            <Button onClick={() => navigate(-1)} variant="outline">
              Go Back
            </Button>
          </div>
        </div>
    );
  }

  const handleSubmit = async () => {
    if (!title.trim()) return;

    const draft = {
      kind,
      title,
      content,
      doi: isPaper ? doi : undefined,
      url: isPaper ? pdfUrl : undefined,
    };

    try {
      setLoading(true);
      if (isEditMode) {
        await publicationApi.editDraft(pubId, draft);
        toast.success("Draft saved");
      } else {
        const res = await publicationApi.createDraft(draft);
        toast.success("Draft created");
        navigate(`/my-publications/${res.pubId}/edit`);
      }
    } catch {
      // handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="min-h-screen bg-background">
        {loading && (
            <LoadingOverlay
                message={isEditMode ? "Saving draft..." : "Creating draft..."}
            />
        )}

        {/* Back Navigation */}
        <div className="max-w-6xl mx-auto px-6 pt-8">
          <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate("/my-publications")}
              className="gap-2 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to My Publications
          </Button>
        </div>

        <div className="max-w-5xl mx-auto px-6 pb-16 space-y-12">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="space-y-2">
              <h1 className="text-4xl font-semibold font-ui text-foreground">
                {isEditMode ? "Edit Draft" : "Create Draft"}
              </h1>
              <KindBadge kind={kind}/>
            </div>
          </div>

          {/* Title */}
          <div className="space-y-2">
            <Label htmlFor="title" className="font-content text-lg font-medium text-foreground">
              Title
            </Label>
            <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="text-lg py-3 font-content"
            />
          </div>

          {/* Paper Metadata */}
          {isPaper && (
              <div className="space-y-6">
                <div className="space-y-2">
                  <Label className="font-ui text-lg font-medium text-foreground">
                    Abstract</Label>
                  <MinimalTiptapEditor
                      key={kind}
                      value={content}
                      onChange={(html) => setContent(html)}
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="doi" className="font-ui text-lg font-medium text-foreground">
                      Doi
                    </Label>
                    <Input
                        id="doi"
                        value={doi}
                        onChange={(e) => setDoi(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="pdf" className="font-ui text-lg font-medium text-foreground">
                      Pdf Url
                    </Label>
                    <Input
                        id="pdf"
                        value={pdfUrl}
                        onChange={(e) => setPdfUrl(e.target.value)}
                    />
                  </div>
                </div>
              </div>
          )}

          {/* Blog/Article Content */}
          {isBlogLike && (
              <div className="space-y-2">
                <Label className="font-ui text-lg font-medium text-foreground">
                  Content</Label>
                <MinimalTiptapEditor
                    key={kind}
                    value={content}
                    onChange={(html) => setContent(html)}
                />
              </div>
          )}

          {/* Footer Submit */}
          <div className="flex items-center justify-between pt-6 border-t border-border">
            <Button variant="outline" onClick={() => navigate(-1)}>
              Cancel
            </Button>

            <Button
                onClick={handleSubmit}
                disabled={!title.trim() || loading}
                size="lg"
                className="gap-2"
            >
              {isEditMode ? <Save className="h-4 w-4" /> : <Plus className="h-4 w-4" />}
              {isEditMode ? "Save Changes" : "Create Draft"}
            </Button>
          </div>
        </div>
      </div>
  );
}