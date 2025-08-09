import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { collectionApi } from "@/lib/api";
import { CollectionCard } from "@/components/CollectionCard.jsx";
import { Button } from "@/components/button.jsx";
import { Plus, Trash2 } from "lucide-react";
import {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogFooter,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogCancel,
  AlertDialogAction,
} from "@/components/alert-dialog.jsx";
import { cn } from "@/lib/utils";

export default function MyCollectionsPage() {
  const navigate = useNavigate();
  const [collections, setCollections] = useState([]);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    setIsInitialLoading(true);
    collectionApi.getMine()
    .then(setCollections)
    .catch(() => toast.error("Failed to fetch your collections."))
    .finally(() => setIsInitialLoading(false));
  }, []);

  const handleDelete = async () => {
    if (!deletingId) return;
    try {
      await collectionApi.delete(deletingId);
      setCollections((prev) =>
          prev.filter((c) => c.collectionId !== deletingId)
      );
      toast.success("Collection deleted.");
    } catch {
      toast.error("Failed to delete collection.");
    } finally {
      setDeletingId(null);
    }
  };

  const defaultCollection = collections.find((c) => c.isDefault);
  const otherCollections = collections.filter((c) => !c.isDefault);

  return (
      <section className="min-h-screen bg-background py-16 px-6">
        <div className="max-w-5xl mx-auto space-y-12">
          {/* page header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-semibold font-ui text-foreground mb-2">
                Your Collections
              </h1>
              <p className="text-sm text-muted-foreground font-ui">
                Create, organize, and share saved publications
              </p>
            </div>
            <Button onClick={() => navigate("/my-collections/new")}>
              <Plus className="w-4 h-4 mr-2" />
              New Collection
            </Button>
          </div>

          {/* default collection */}
          <div className="space-y-8">
            {defaultCollection && (
                <div
                    className={cn(
                        "transition-opacity duration-500 ease-in-out",
                        isInitialLoading ? "opacity-0" : "opacity-100"
                    )}
                >
                  <h2 className="font-ui font-semibold text-muted-foreground mb-2 text-sm uppercase tracking-wide">
                    Default Collection
                  </h2>
                  <CollectionCard collection={defaultCollection} />
                </div>
            )}

            {/* collection grid */}
            <div
                className={cn(
                    "space-y-4 transition-opacity duration-500 ease-in-out",
                    isInitialLoading ? "opacity-0" : "opacity-100"
                )}
            >
              <h2 className="font-ui font-semibold text-muted-foreground mb-2 text-sm uppercase tracking-wide">
                Your Collections
              </h2>

              <div
                  className={cn(
                      "grid gap-4 grid-cols-1 sm:grid-cols-2 min-h-[135px] transition-opacity duration-500 ease-in-out",
                      isRefreshing && "opacity-50 pointer-events-none"
                  )}
              >
                {isInitialLoading ? null : otherCollections.length > 0 ? (
                    otherCollections.map((c) => (
                        <CollectionCard
                            key={c.collectionId}
                            collection={c}
                            variant="compact"
                            onEdit={() =>
                                navigate(`/my-collections/${c.collectionId}/edit`)
                            }
                            onDelete={() => setDeletingId(c.collectionId)}
                        />
                    ))
                ) : (
                    <p className="text-muted-foreground font-content text-center col-span-full py-8">
                      You haven't created any collections yet.
                    </p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* delete confirmation dialog */}
        <AlertDialog
            open={!!deletingId}
            onOpenChange={(open) => !open && setDeletingId(null)}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete Collection?</AlertDialogTitle>
              <AlertDialogDescription>
                This action cannot be undone. This will permanently delete your
                collection and all its associated data.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                  onClick={handleDelete}
                  className="bg-destructive/10 text-destructive hover:bg-destructive/20"
              >
                <Trash2 className="w-4 h-4 mr-2" />
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </section>
  );
}