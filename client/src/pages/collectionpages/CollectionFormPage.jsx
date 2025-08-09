import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { collectionApi, publicationApi } from "@/lib/api";
import { Button } from "@/components/button.jsx";
import { Input } from "@/components/input.jsx";
import { Label } from "@/components/label.jsx";
import { Plus, ArrowLeft, Search, X, Eye, EyeOff, Save } from "lucide-react";
import MinimalTiptapEditor from "@/components/editor/minimal-tiptap.jsx";
import { cn } from "@/lib/utils";

export default function CollectionFormPage() {
  const navigate = useNavigate();
  const { collectionId } = useParams();
  const isEditing = !!collectionId;

  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [showContent, setShowContent] = useState(!isEditing);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(false);
  const [publications, setPublications] = useState([]);

  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);

  useEffect(() => {
    if (!isEditing) return;

    const loadCollection = async () => {
      try {
        const response = await collectionApi.getById(collectionId);
        setTitle(response.title);
        setDescription(response.description || "");
        setIsPublic(response.isPublic || false);
        setPublications(response.publications || []);
      } catch {
        toast.error("Failed to load collection data.");
        navigate("/my-collections");
      } finally {
        setLoading(false);
      }
    };

    loadCollection();
  }, [isEditing, collectionId, navigate]);

  useEffect(() => {
    if (!loading && !showContent && isEditing) {
      const timer = setTimeout(() => {
        setShowContent(true);
      }, 50);

      return () => clearTimeout(timer);
    }
  }, [loading, showContent, isEditing]);

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    try {
      const results = await publicationApi.search(searchTerm);
      const filtered = results.filter(r => !publications.some(p => p.pubId === r.pubId));
      setSearchResults(filtered);
    } catch {
      toast.error("Failed to search for publications.");
    }
  };

  const addPublication = async (pub) => {
    if (isEditing) {
      try {
        await collectionApi.addToCollection(collectionId, pub.pubId);
        setPublications(prev => [...prev, pub]);
        setSearchResults(prev => prev.filter(p => p.pubId !== pub.pubId));
        toast.success(`Added "${pub.title}" to collection.`);
      } catch {
        toast.error("Failed to add publication.");
      }
    } else {
      setPublications(prev => [...prev, pub]);
      setSearchResults(prev => prev.filter(p => p.pubId !== pub.pubId));
    }
  };

  const removePublication = async (pub) => {
    if (isEditing) {
      try {
        await collectionApi.removeFromCollection(collectionId, pub.pubId);
        setPublications(prev => prev.filter(p => p.pubId !== pub.pubId));
        toast.success(`Removed "${pub.title}" from collection.`);
      } catch {
        toast.error("Failed to remove publication.");
      }
    } else {
      setPublications(prev => prev.filter(p => p.pubId !== pub.pubId));
    }
  };

  const handleSave = async () => {
    if (!title.trim()) return toast.error("Title is required.");
    setSaving(true);

    const collectionData = { title, description, isPublic };

    try {
      if (isEditing) {
        await collectionApi.update(collectionId, collectionData);
        toast.success("Collection updated successfully.");
      } else {
        const newCollection = await collectionApi.create(collectionData);
        toast.success("Collection created successfully.");
        for (const pub of publications) {
          await collectionApi.addToCollection(newCollection.collectionId, pub.pubId);
        }
        navigate(`/my-collections/${newCollection.collectionId}/edit`);
        return;
      }
    } catch {
      toast.error("Failed to save collection.");
    } finally {
      setSaving(false);
    }
  };

  return (
      <div className="min-h-screen bg-background">
        {/* back to my collections */}
        <div className="max-w-6xl mx-auto px-6 pt-8">
          <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate("/my-collections")}
              className="gap-2 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to My Collections
          </Button>
        </div>

        {/* form body */}
        <div
            className={cn(
                "max-w-5xl mx-auto px-6 pb-16 space-y-12 transition-opacity duration-500 ease-in-out",
                showContent ? "opacity-100" : "opacity-0 pointer-events-none"
            )}
        >
          <h1 className="text-4xl font-semibold font-ui text-foreground">
            {isEditing ? "Edit Collection" : "Create Collection"}
          </h1>

          {/* collection title input */}
          <div className="space-y-2">
            <Label htmlFor="title" className="font-content text-lg font-medium">
              Title
            </Label>
            <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="text-lg py-3"
            />
          </div>

          {/* collection description mte */}
          <div className="space-y-2">
            <Label className="font-ui text-lg font-medium">Description</Label>
            {!loading && (
                <MinimalTiptapEditor
                    value={description}
                    onChange={(html) => setDescription(html)}
                />
            )}
          </div>

          {/* visibility toggle */}
          <div className="space-y-2">
            <Label className="font-ui text-lg font-medium">Visibility</Label>
            <Button
                variant="outline"
                className="w-full md:w-auto justify-start gap-2"
                onClick={() => setIsPublic(!isPublic)}
            >
              {isPublic ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
              {isPublic ? "Public: Visible to everyone" : "Private: Only you can see"}
            </Button>
          </div>

          {/* choose publications section */}
          <div className="space-y-6">
            <h2 className="text-2xl font-semibold">Publications</h2>
            <div className="flex gap-2">
              <Input
                  placeholder="Search for publications..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
              <Button onClick={handleSearch} disabled={loading}>
                <Search className="h-4 w-4 mr-2" />
                Search
              </Button>
            </div>

            {/* pub results */}
            {searchResults.length > 0 && (
                <div className="space-y-2 p-4 border rounded-lg">
                  <h3 className="font-semibold text-sm">Search Results</h3>
                  {searchResults.map((pub) => (
                      <div
                          key={pub.pubId}
                          className="flex items-center justify-between text-sm p-2 rounded-md hover:bg-muted"
                      >
                        <span>{pub.title}</span>
                        <Button size="sm" variant="ghost" onClick={() => addPublication(pub)}>
                          <Plus className="h-4 w-4" />
                        </Button>
                      </div>
                  ))}
                </div>
            )}

            {/* chosen publications */}
            <div className="space-y-2">
              <h3 className="font-semibold">
                Added to Collection ({publications.length})
              </h3>
              {publications.length > 0 ? (
                  publications.map((pub) => (
                      <div
                          key={pub.pubId}
                          className="flex items-center justify-between p-2 border rounded-md bg-muted/50"
                      >
                        <span>{pub.title}</span>
                        <Button
                            size="sm"
                            variant="ghost"
                            className="text-destructive"
                            onClick={() => removePublication(pub)}
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                  ))
              ) : (
                  <p className="text-sm text-muted-foreground">
                    No publications added yet.
                  </p>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between pt-6 border-t border-border">
            <Button variant="outline" onClick={() => navigate(-1)}>
              Cancel
            </Button>
            <Button
                onClick={handleSave}
                disabled={!title.trim() || saving || loading}
                size="lg"
                className="gap-2"
            >
              {saving ? "Saving..." : (isEditing ? <>
                <Save className="h-4 w-4" /> Save Changes
              </> : <>
                <Plus className="h-4 w-4" /> Create Collection
              </>)}
            </Button>
          </div>
        </div>
      </div>
  );
}